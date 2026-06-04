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
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Duration;
import java.util.List;


@Slf4j
@Component
public class AggregationStarter implements CommandLineRunner {

    private final KafkaConsumer<Long, UserActionAvro> consumer;
    private final KafkaProducer<Long, EventSimilarityAvro> producer;
    private final EventSimilarityService eventSimilarityService;

    private final KafkaConfig kafkaConfig;
    private static final Duration POLL_DURATION = Duration.ofMillis(100);


    public AggregationStarter(KafkaConfig kafkaConfig,
                              EventSimilarityService eventSimilarityService) {
        this.kafkaConfig = kafkaConfig;
        this.producer = new KafkaProducer<>(kafkaConfig.getProducerProperties());
        this.consumer = new KafkaConsumer<>(kafkaConfig.getConsumerProperties());
        this.eventSimilarityService = eventSimilarityService;
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

                if (records.isEmpty()) continue;

                if (records.count() > 0) {
                    log.debug("Processing {} user actions", records.count());
                }

                for (ConsumerRecord<Long, UserActionAvro> record : records) {
                    List<EventSimilarityAvro> eventSimilarityAvros = eventSimilarityService.updateEventSimilarity(record.value());
                    if (eventSimilarityAvros.isEmpty()) continue;
                    log.debug("Sending {} event similarities", eventSimilarityAvros.size());
                    eventSimilarityAvros.forEach(this::sendEventSimilarity);
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
}
