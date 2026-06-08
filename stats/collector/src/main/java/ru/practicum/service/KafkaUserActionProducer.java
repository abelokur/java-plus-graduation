package ru.practicum.service;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Component;
import ru.practicum.config.KafkaConfig;
import ru.practicum.config.TopicType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

/**
 * Кастомная реализация KafkaProducer с функцией отправки событий в топики.
 * Управляет жизненным циклом Kafka producer и автоматически закрывает его при завершении работы приложения.
 *
 * @see AutoCloseable
 */
@Slf4j
@Component
public class KafkaUserActionProducer implements AutoCloseable {
    protected final KafkaProducer<Long, SpecificRecordBase> producer;
    protected final EnumMap<TopicType, String> topics;

    /**
     * Конструктор класса для создания KafkaProducer
     * и заполнения всеми известными топиками
     *
     * @param kafkaConfig базовая конфигурация Kafka
     */
    public KafkaUserActionProducer(KafkaConfig kafkaConfig) {
        topics = kafkaConfig.getTopics();
        producer = new KafkaProducer<>(kafkaConfig.getProducerProperties());
    }

    /**
     * Отправка события в Kafka
     *
     * @param topicType  тип топика из enum
     * @param key        ключ для партицирования
     * @param userAction событие в формате Avro
     */
    public <T extends SpecificRecordBase> void sendEvent(TopicType topicType, Long key, T userAction) {
        String topicName = getTopicName(topicType);

        ProducerRecord<Long, SpecificRecordBase> record =
                new ProducerRecord<>(topicName, key, userAction);

        producer.send(record, (metadata, exception) -> {
            if (exception != null) {
                log.error("Failed to send userAction: {} to topic: {}, Key: {}",
                        userAction, topicName, key, exception);
            } else {
                log.debug("UserAction: {} successfully sent to topic: {}, Key: {}, Partition: {}, Offset: {}",
                        userAction, topicName, key, metadata.partition(), metadata.offset());
            }
        });
    }

    /**
     * Получение имени топика по типу
     *
     * @param topicType тип топика
     * @return String имя топика
     * @throws IllegalArgumentException если тип топика неизвестен
     */
    public String getTopicName(TopicType topicType) {
        String topicName = topics.get(topicType);
        if (topicName == null) {
            throw new IllegalArgumentException("Unknown topic type: " + topicType);
        }
        return topicName;
    }

    /**
     * Получение всех топиков (для инициализации)
     *
     * @return List список всех имен топиков
     */
    public List<String> getAllTopicNames() {
        return new ArrayList<>(topics.values());
    }

    /**
     * Принудительная отправка всех накопленных сообщений
     */
    public void flush() {
        producer.flush();
    }

    /**
     * Закрывает Kafka producer и освобождает ресурсы.
     * Автоматически вызывается Spring при завершении работы приложения.
     */
    @PreDestroy
    @Override
    public void close() {
        if (producer != null) {
            try {
                log.info("Closing KafkaUserActionProducer...");
                producer.close();
                log.info("KafkaUserActionProducer closed successfully");
            } catch (Exception e) {
                log.error("Error closing KafkaUserActionProducer", e);
            }
        }
    }
}
