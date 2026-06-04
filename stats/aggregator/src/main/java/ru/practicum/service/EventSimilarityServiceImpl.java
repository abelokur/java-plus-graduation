package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.config.WeightConfig;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.repository.InMemoryRepository;

import java.time.Instant;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventSimilarityServiceImpl implements EventSimilarityService {

    private final InMemoryRepository inmemoryRepository;
    private final WeightConfig weightConfig;

    @Override
    public List<EventSimilarityAvro> updateEventSimilarity(UserActionAvro userAction) {
        if (userAction == null) {
            return Collections.emptyList();
        }

        log.debug("Updating score for userAction {}", userAction);
        List<EventSimilarityAvro> eventSimilarityAvros = new ArrayList<>();

        long userId = userAction.getUserId();
        long eventId = userAction.getEventId();

        // Получаем Weight для eventId и userId
        double oldWeight = inmemoryRepository.getEventUserWeight(eventId, userId);
        double newWeight = getWeight(userAction.getActionType());

        log.debug("oldWeight: {}, newWeight: {}", oldWeight, newWeight);

        if (newWeight <= oldWeight) {
            log.debug("Weight not increased ({} <= {}). Skipping similarity calculations.",
                    newWeight, oldWeight);
            return Collections.emptyList();
        }

        // 1. Обновляем вес пользователя для события
        inmemoryRepository.putEventUserWeight(eventId, userId, newWeight);

        // 2. Обновляем сумму весов события
        double oldEventWeightSum = inmemoryRepository.getEventWeightSum(eventId);
        inmemoryRepository.putEventWeightSum(eventId, (oldEventWeightSum - oldWeight + newWeight));
        log.debug("oldEventWeightSum: {}, newEventWeightSum: {}", oldEventWeightSum, inmemoryRepository.getEventWeightSum(eventId));

        // 3. Получаем другие события пользователя
        Set<Long> userEvents = new HashSet<>(inmemoryRepository.getUserEvents(userId));
        userEvents.remove(eventId);

        log.debug("userEvents: {}", userEvents);
        // 4. Для каждого другого события пользователя
        for (Long otherEventId : userEvents) {
            double otherWeight = inmemoryRepository.getEventUserWeight(otherEventId, userId);

            double oldMin = Math.min(oldWeight, otherWeight);
            double newMin = Math.min(newWeight, otherWeight);

            // Если минимум изменился
            if (newMin != oldMin) {
                // Получаем текущую сумму и обновляем ее
                double oldMinSum = inmemoryRepository.getMinWeightSum(eventId, otherEventId);
                double newMinSum = oldMinSum - oldMin + newMin;

                inmemoryRepository.putMinWeightSum(eventId, otherEventId, newMinSum);

                log.debug("Pair ({}, {}): oldWeight={}, newWeight={}, otherWeight={}, oldMin={}, newMin={}",
                        eventId, otherEventId, oldWeight, newWeight, otherWeight, oldMin, newMin);
            }

            // 5. Вычисляем схожесть
            double minWeightSum = inmemoryRepository.getMinWeightSum(eventId, otherEventId);
            double eventWeightSum = inmemoryRepository.getEventWeightSum(eventId);
            double otherEventWeightSum = inmemoryRepository.getEventWeightSum(otherEventId);

            log.debug("Pair ({}, {}): minWeightSum={}, eventWeightSum={}, otherEventWeightSum={}",
                    eventId, otherEventId, minWeightSum, eventWeightSum, otherEventWeightSum);

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

        // 7. Добавляем событие в историю пользователя (только если вес > 0)
        if (newWeight > 0) {
            inmemoryRepository.putUserEvent(userId, eventId);
        }

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

        log.debug("eventSimilarity: {}, minWeightSum: {}, eventWeightSqrt1: {}, eventWeightSqrt2: {}",
                eventSimilarity, minWeightSum, eventWeightSqrt1, eventWeightSqrt2);

        return eventSimilarity;
    }
}
