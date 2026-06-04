package ru.practicum.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.practicum.config.KafkaConfig;
import ru.practicum.config.TopicType;
import ru.practicum.config.WeightConfig;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.repository.InMemoryRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.*;


@Slf4j
@Component
public class AggregationStarter implements CommandLineRunner {

    private final KafkaConsumer<Long, UserActionAvro> consumer;
    private final KafkaProducer<Long, EventSimilarityAvro> producer;

    private final InMemoryRepository inmemoryRepository;

    private final KafkaConfig kafkaConfig;
    private final WeightConfig weightConfig;
    private static final Duration POLL_DURATION = Duration.ofSeconds(3);


    public AggregationStarter(KafkaConfig kafkaConfig,
                              WeightConfig weightConfig,
                              InMemoryRepository inmemoryRepository) {
        this.kafkaConfig = kafkaConfig;
        this.weightConfig = weightConfig;
        this.producer = new KafkaProducer<>(kafkaConfig.getProducerProperties());
        this.consumer = new KafkaConsumer<>(kafkaConfig.getConsumerProperties());
        this.inmemoryRepository = inmemoryRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting aggregation service via CommandLineRunner");
        start();
    }


    public void start() {
        log.info("Starting aggregation service with empty initial state");
        String topic = kafkaConfig.getTopics().get(TopicType.USER_ACTION);

        try (consumer) {
            Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
            consumer.subscribe(List.of(topic));

            while (true) {
                ConsumerRecords<Long, UserActionAvro> records = consumer.poll(POLL_DURATION);

                if (records.count() > 0) {
                    log.debug("Processing {} user actions", records.count());
                }

                for (ConsumerRecord<Long, UserActionAvro> record : records) {
                    updateScore(record.value()).forEach(this::sendEventSimilarity);
                }

                consumer.commitSync();
            }
        } catch (WakeupException ignored) {
            log.info("Wakeup interrupted");
        } catch (Exception e) {
            log.error("Exception while trying to handle user action data", e);
        } finally {
            try {
                if (producer != null) {
                    producer.flush();
                }
                if (consumer != null) {
                    consumer.commitSync();
                }
            } finally {
                log.info("Closing consumer");
                if (consumer != null) {
                    consumer.close();
                }
                log.info("Closing producer");
                if (producer != null) {
                    producer.close();
                }
            }
        }
    }

    private List<EventSimilarityAvro> updateScore(UserActionAvro userAction) {
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

        if (newWeight <= oldWeight) {
            return Collections.emptyList();
        }

        // 1. Обновляем вес пользователя для события
        inmemoryRepository.putEventUserWeight(eventId, userId, newWeight);

        // 2. Обновляем сумму весов события
        double oldEventWeightSum = inmemoryRepository.getEventWeightSum(eventId);
        inmemoryRepository.putEventWeightSum(eventId, (oldEventWeightSum - oldWeight + newWeight));

        // 3. Получаем другие события пользователя
        Set<Long> userEvents = new HashSet<>(inmemoryRepository.getUserEvents(userId));
        userEvents.remove(eventId);

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


    private void sendEventSimilarity(EventSimilarityAvro eventSimilarity) {
        String topicName = kafkaConfig.getTopics().get(TopicType.EVENT_SIMILARITY);

        ProducerRecord<Long, EventSimilarityAvro> record =
                new ProducerRecord<>(topicName, eventSimilarity);

        log.debug("Sending eventSimilarity {} to topic {}", eventSimilarity, topicName);

        producer.send(record, (metadata, exception) -> {
            if (exception != null) {
                log.error("Failed to send eventSimilarity: {} to topic: {}",
                        eventSimilarity, topicName, exception);
            } else {
                log.debug("EventSimilarity: {} successfully sent to topic: {}, Partition: {}, Offset: {}",
                        eventSimilarity, topicName, metadata.partition(), metadata.offset());
            }
        });
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
        double productOfSqrtEventWeights =
                Math.sqrt(eventAWeightSum) *
                Math.sqrt(eventBWeightSum);
        if (productOfSqrtEventWeights == 0.0) {
            return 0.0;
        }
        return minWeightSum / productOfSqrtEventWeights;
    }
}
