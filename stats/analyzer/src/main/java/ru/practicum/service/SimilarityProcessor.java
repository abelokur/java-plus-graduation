package ru.practicum.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.config.KafkaConfig;
import ru.practicum.config.TopicType;
import ru.practicum.dal.service.SimilarityService;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
public class SimilarityProcessor implements Runnable {

    private final KafkaConsumer<Long, EventSimilarityAvro> consumer;
    private final SimilarityService similarityService;

    private final KafkaConfig kafkaConfig;
    private static final Duration POLL_DURATION = Duration.ofMillis(100);


    public SimilarityProcessor(KafkaConfig kafkaConfig,
                               SimilarityService similarityService) {
        this.kafkaConfig = kafkaConfig;
        this.consumer = new KafkaConsumer<>(kafkaConfig.getEventSimilarityConsumerProperties());
        this.similarityService = similarityService;
    }

    @Override
    public void run() {
        log.info("Starting similarity processor");
        String topic = kafkaConfig.getTopics().get(TopicType.EVENT_SIMILARITY);

        try (consumer) {
            Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
            consumer.subscribe(List.of(topic));

            while (true) {
                ConsumerRecords<Long, EventSimilarityAvro> records = consumer.poll(POLL_DURATION);

                if (records.count() > 0) {
                    log.debug("Processing {} event similarities", records.count());
                }

                for (ConsumerRecord<Long, EventSimilarityAvro> record : records) {
                    log.debug("Processing event similarity {}", record.value());
                    similarityService.saveOrUpdate(record.value());
                }

                consumer.commitSync();
            }
        } catch (WakeupException ignored) {
            log.info("Wakeup interrupted");
        } catch (Exception e) {
            log.error("Exception while trying to handle event similarity data", e);
        }
    }
}
