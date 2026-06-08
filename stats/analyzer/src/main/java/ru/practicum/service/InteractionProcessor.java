package ru.practicum.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.config.KafkaConfig;
import ru.practicum.config.TopicType;
import ru.practicum.dal.service.InteractionService;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Duration;
import java.util.List;


@Slf4j
@Component
public class InteractionProcessor implements Runnable {

    private final KafkaConsumer<Long, UserActionAvro> consumer;
    private final InteractionService interactionService;

    private final KafkaConfig kafkaConfig;
    private static final Duration POLL_DURATION = Duration.ofMillis(100);


    public InteractionProcessor(KafkaConfig kafkaConfig,
                                InteractionService interactionService) {
        this.kafkaConfig = kafkaConfig;
        this.consumer = new KafkaConsumer<>(kafkaConfig.getUserActionConsumerProperties());
        this.interactionService = interactionService;
    }

    @Override
    public void run() {
        log.info("Starting interaction processor");
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
                    log.debug("Processing user action {}", record.value());
                    interactionService.saveOrUpdate(record.value());
                }

                consumer.commitSync();
            }
        } catch (WakeupException ignored) {
            log.info("Wakeup interrupted");
        } catch (Exception e) {
            log.error("Exception while trying to handle user action data", e);
        }
    }
}
