package ru.practicum.dal.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.dal.model.RecommendedEventProjection;
import ru.practicum.dal.model.mapper.Mapper;
import ru.practicum.dal.repository.InteractionRepository;
import ru.practicum.dal.repository.SimilarityRepository;
import ru.practicum.ewm.stats.proto.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.ewm.stats.proto.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.proto.UserPredictionsRequestProto;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {
    private final InteractionRepository interactionRepository;
    private final SimilarityRepository similarityRepository;
    private final Mapper mapper;

    @Override
    public List<RecommendedEventProto> getRecommendationsForUser(UserPredictionsRequestProto request) {
        List<Long> interactedEvents = interactionRepository
                .getRecentUserEvents(request.getUserId(), request.getMaxResults());

        if (interactedEvents.isEmpty()) {
            return Collections.emptyList();
        }

        return similarityRepository.findRecommendedEvents(interactedEvents, request.getMaxResults()).stream()
                .map(RecommendedEventProjection::toProto)
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
}
