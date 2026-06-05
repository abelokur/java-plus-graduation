package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.config.WeightConfig;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.repository.BaseRepository;

import java.time.Instant;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventSimilarityServiceImpl implements EventSimilarityService {

    private final BaseRepository repository;
    private final WeightConfig weightConfig;

    @Override
    public List<EventSimilarityAvro> updateEventSimilarity(UserActionAvro userAction) {
        if (userAction == null) {
            return Collections.emptyList();
        }

        log.debug("Updating similarity for userAction {}", userAction);

        List<EventSimilarityAvro> eventSimilarityAvros = new ArrayList<>();

        long userId = userAction.getUserId();
        long eventId = userAction.getEventId();

        double oldWeight = repository.getEventUserWeight(eventId, userId);
        double newWeight = getWeight(userAction.getActionType());
        double deltaWeight = newWeight - oldWeight;


        if (deltaWeight <= 0) {
            log.debug("Weight not increased ({} <= {}). Skipping similarity calculations.",
                    newWeight, oldWeight);
            repository.putUserEvent(userId, eventId);
            return Collections.emptyList();
        }

        // 1. Обновляем вес пользователя для события
        repository.putEventUserWeight(eventId, userId, newWeight);

        // 2. Обновляем сумму весов события
        double oldEventWeightSum = repository.getEventWeightSum(eventId);
        double newEventWeightSum = oldEventWeightSum + deltaWeight;
        repository.putEventWeightSum(eventId, newEventWeightSum);
        log.debug("oldEventWeightSum: {}, newEventWeightSum: {}", oldEventWeightSum, newEventWeightSum);

        // 3. Получаем другие события пользователя
        Set<Long> userEvents = new HashSet<>(repository.getUserEvents(userId));
        userEvents.remove(eventId);
        log.debug("Need to calculate [{}] similarities", userEvents.size());

        // 4. Для каждого другого события пользователя
        for (Long otherEventId : userEvents) {
            double otherWeight = repository.getEventUserWeight(otherEventId, userId);

            double oldMin = Math.min(oldWeight, otherWeight);
            double newMin = Math.min(newWeight, otherWeight);
            double deltaMin = newMin - oldMin;

            // Если минимум изменился (по ТЗ может только увеличиться)
            if (deltaMin > 0) {
                // Получаем текущую сумму и обновляем ее
                double oldMinSum = repository.getMinWeightSum(eventId, otherEventId);
                double newMinSum = oldMinSum + deltaMin;

                repository.putMinWeightSum(eventId, otherEventId, newMinSum);

                log.debug("Pair events ({}, {}): oldWeight={}, newWeight={}, oldMin={}, newMin={}",
                        eventId, otherEventId, oldWeight, newWeight, oldMin, newMin);
            }

            // 5. Вычисляем схожесть
            double minWeightSum = repository.getMinWeightSum(eventId, otherEventId);
            double eventWeightSum = repository.getEventWeightSum(eventId);
            double otherEventWeightSum = repository.getEventWeightSum(otherEventId);

            double similarity = calculateSimilarity(minWeightSum, eventWeightSum, otherEventWeightSum);

            // 6. Создаем объект схожести только если нужно
            if (similarity > 0) {
                eventSimilarityAvros.add(createEventSimilarity(
                        eventId,
                        otherEventId,
                        similarity,
                        userAction.getTimestamp()));

                log.debug("New similarity between event {} and event {} is {}",
                        eventId, otherEventId, similarity);
            }
        }

        // 7. Добавляем событие в историю пользователя
        repository.putUserEvent(userId, eventId);

        return eventSimilarityAvros;
    }

    private double getWeight(ActionTypeAvro actionType) {
        return switch (actionType) {
            case VIEW -> weightConfig.getView();
            case REGISTER -> weightConfig.getRegister();
            case LIKE -> weightConfig.getLike();
        };
    }

    private EventSimilarityAvro createEventSimilarity(Long eventA, Long eventB, double similarity, Instant timestamp) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        return EventSimilarityAvro.newBuilder()
                .setEventA(first)
                .setEventB(second)
                .setScore(similarity)
                .setTimestamp(timestamp)
                .build();
    }

    private double calculateSimilarity(double minWeightSum, double eventAWeightSum, double eventBWeightSum) {
        if (minWeightSum == 0.0 || eventAWeightSum == 0.0 || eventBWeightSum == 0.0) {
            return 0.0;
        }

        double eventWeightSqrt1 = Math.sqrt(eventAWeightSum);
        double eventWeightSqrt2 = Math.sqrt(eventBWeightSum);
        double eventSimilarity = minWeightSum / (eventWeightSqrt1 * eventWeightSqrt2);

        log.debug("Calculating Event Similarity: similarity = {}, MinWeightSum = {}, WeightSqrt1 = {}, WeightSqrt2 = {}",
                eventSimilarity, minWeightSum, eventWeightSqrt1, eventWeightSqrt2);

        return eventSimilarity;
    }
}
