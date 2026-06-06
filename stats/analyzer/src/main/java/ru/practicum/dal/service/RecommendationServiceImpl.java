package ru.practicum.dal.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.config.AppConfig;
import ru.practicum.dal.model.RecommendedEventProjection;
import ru.practicum.dal.repository.InteractionRepository;
import ru.practicum.dal.repository.SimilarityRepository;
import ru.practicum.ewm.stats.proto.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.ewm.stats.proto.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.proto.UserPredictionsRequestProto;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RecommendationServiceImpl implements RecommendationService {
    private final InteractionRepository interactionRepository;
    private final SimilarityRepository similarityRepository;
    private final AppConfig appConfig;

    private final int maxInteractions;
    private final int maxNearby;

    public RecommendationServiceImpl(InteractionRepository interactionRepository,
                                     SimilarityRepository similarityRepository,
                                     AppConfig appConfig) {
        this.interactionRepository = interactionRepository;
        this.similarityRepository = similarityRepository;
        this.appConfig = appConfig;
        this.maxInteractions = appConfig.getEventSimilarity().getMaxInteractions();
        this.maxNearby = appConfig.getEventSimilarity().getMaxNearby();
    }

    @Override
    public List<RecommendedEventProto> getRecommendationsForUser(UserPredictionsRequestProto request) {
        // 1. Находим недавние maxInteractions события с которыми взаимодействовал пользователь, с оценками (Будем использовать для расчетов)
        long userId = request.getUserId();
        Map<Long, Double> interactedEvents = getRecentInteractedEvents(userId);

        if (interactedEvents.isEmpty()) {
            log.debug("User: {} has no any interactions", userId);
            return Collections.emptyList();
        }

        List<Long> interactedEventIds = new ArrayList<>(interactedEvents.keySet());

        // 2. Находим похожие на отобранные на 1ом шаге события (ограничение maxResult)
        Map<Long, Double> recommendedEvents = getRecommendedEvents(interactedEventIds, request.getMaxResults());

        if (recommendedEvents.isEmpty()) {
            log.warn("User: {} has no any recommended events", userId);
            return Collections.emptyList();
        }
        log.debug("User {} has recommended events: {}", userId, recommendedEvents.keySet());

        Map<Long, Double> result = new HashMap<>();

        // 3. По каждому событию находим maxNearby ближайших событий с которыми взаимодействовал пользователь
        // необходимости выгружать все interactions пользователя нет,
        // будем использовать события пользователей, по которым происходил поиск рекомендаций
        for (Long eventId : recommendedEvents.keySet()) {
            log.debug("Searching nearby events for {}", eventId);
            Map<Long, Double> nearbyEvents = getNearbyEvents(eventId, interactedEventIds);
            log.debug("Found [{}] nearby for: {}", nearbyEvents.size(), eventId);

            double prediction = calculatePredictedScore(interactedEvents, nearbyEvents);

            // Если предсказать оценку не удалось, выводим похожесть мероприятия
            if (prediction == 0.0) {
                log.warn("No prediction for {}", eventId);
                result.put(eventId, recommendedEvents.get(eventId));
            } else {
                result.put(eventId, prediction);
            }
        }

        return result.entrySet().stream()
                .map(entry -> RecommendedEventProto.newBuilder()
                        .setEventId(entry.getKey())
                        .setScore(entry.getValue())
                        .build())
                .toList();
    }

    @Override
    public List<RecommendedEventProto> getSimilarEvents(SimilarEventsRequestProto request) {
        Set<Long> interactedEvents = new HashSet<>(interactionRepository
                .findAllUserEvents(request.getUserId()));
        interactedEvents.add(request.getEventId());

        return similarityRepository.findSimilarEvents(
                        request.getEventId(),
                        new ArrayList<>(interactedEvents),
                        request.getMaxResults())
                .stream()
                .map(RecommendedEventProjection::toProto)
                .toList();
    }

    @Override
    public List<RecommendedEventProto> getInteractionsCount(InteractionsCountRequestProto request) {
        Set<Long> eventIds = new HashSet<>(request.getEventIdList());
        return interactionRepository.getAggregatedRatingByEvent(eventIds).stream()
                .map(RecommendedEventProjection::toProto)
                .toList();
    }

    private Map<Long, Double> getNearbyEvents(Long eventId, List<Long> interactedEventIds) {
        return similarityRepository.findNearbyEvents(
                        eventId,
                        interactedEventIds,
                        maxNearby)
                .stream()
                .collect(Collectors.toMap(
                        RecommendedEventProjection::getEventId,
                        RecommendedEventProjection::getScore));
    }

    private Map<Long, Double> getRecommendedEvents(List<Long> interactedEventIds, int maxResults) {
        return similarityRepository.findRecommendedEvents(
                        interactedEventIds,
                        maxResults)
                .stream()
                .collect(Collectors.toMap(
                        RecommendedEventProjection::getEventId,
                        RecommendedEventProjection::getScore));
    }

    private Map<Long, Double> getRecentInteractedEvents(long userId) {
        return interactionRepository
                .getRecentUserEvents(userId, maxInteractions)
                .stream()
                .collect(Collectors.toMap(
                        RecommendedEventProjection::getEventId,
                        RecommendedEventProjection::getScore));
    }

    private double calculatePredictedScore(
            Map<Long, Double> interactedEvents,
            Map<Long, Double> nearbyEvents) {

        double numerator = 0.0;
        double denominator = 0.0;

        // 4. Считаем средневзвешенную оценку для каждого события
        for (Map.Entry<Long, Double> entry : nearbyEvents.entrySet()) {
            numerator += entry.getValue() * interactedEvents.get(entry.getKey());
            denominator += entry.getValue();
        }

        if (denominator == 0.0) {
            return 0.0;
        }

        double prediction = numerator / denominator;
        log.debug("Prediction score: {}, numerator: {}, denominator: {}",
                prediction, numerator, denominator);
        return prediction;
    }
}
