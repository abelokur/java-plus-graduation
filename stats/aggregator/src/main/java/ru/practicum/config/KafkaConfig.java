package ru.practicum.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.EnumMap;
import java.util.Map;
import java.util.Properties;

/**
 * Конфигурационный класс для настройки Kafka клиентов.
 * Связывает свойства из конфигурационных файлов (application.yml/properties)
 * с Java объектами для работы с Apache Kafka.
 *
 * @see TopicType
 * @see org.apache.kafka.clients.producer.ProducerConfig
 * @see org.apache.kafka.clients.consumer.ConsumerConfig
 */
@Getter
@Setter
@ToString
@ConfigurationProperties("kafka")
@Slf4j
public class KafkaConfig {
    /**
     * Карта соответствия типов топиков их фактическим названиям в Kafka.
     * Ключ - перечисление {@link TopicType}, значение - строковое название топика.
     *
     * <p>Инициализируется через метод {@link #setTopics(Map)} при загрузке конфигурации.
     *
     * @see TopicType
     */
    private EnumMap<TopicType, String> topics;
    /**
     * Настройки Kafka продюсера.
     * Содержит свойства для конфигурации экземпляра {@link org.apache.kafka.clients.producer.KafkaProducer}.
     *
     * <p>Общие настройки включают:
     * <ul>
     *   <li>{@code bootstrap.servers} - адреса брокеров Kafka</li>
     *   <li>{@code key.serializer} - сериализатор для ключей сообщений</li>
     *   <li>{@code value.serializer} - сериализатор для значений сообщений</li>
     * </ul>
     *
     * @see org.apache.kafka.clients.producer.ProducerConfig
     */
    private Properties producerProperties;

    /**
     * Настройки Kafka консьюмера.
     * Содержит свойства для конфигурации экземпляра {@link org.apache.kafka.clients.consumer.KafkaConsumer}.
     *
     * <p>Общие настройки включают:
     * <ul>
     *   <li>{@code bootstrap.servers} - адреса брокеров Kafka</li>
     *   <li>{@code group.id} - идентификатор потребительской группы</li>
     *   <li>{@code key.deserializer} - десериализатор для ключей сообщений</li>
     *   <li>{@code value.deserializer} - десериализатор для значений сообщений</li>
     *   <li>{@code auto.offset.reset} - стратегия при отсутствии оффсета</li>
     *   <li>{@code enable.auto.commit} - автоматическое подтверждение сообщений</li>
     * </ul>
     *
     * @see org.apache.kafka.clients.consumer.ConsumerConfig
     */
    private Properties consumerProperties;

    /**
     * Устанавливает соответствие между строковыми ключами топиков из конфигурации
     * и перечислением {@link TopicType}.
     *
     * <p>Метод преобразует входную Map<String, String> в EnumMap<TopicType, String>,
     * где ключи конвертируются в значения перечисления {@link TopicType}.
     *
     * <p>Если ключ из конфигурации не соответствует ни одному значению {@link TopicType},
     * выводится предупреждение в лог и такой топик игнорируется.
     *
     * @param topics Map, где ключ - строковое представление {@link TopicType},
     *               значение - название топика в Kafka
     * @throws IllegalArgumentException если ключ не соответствует {@link TopicType}
     *
     */
    public void setTopics(Map<String, String> topics) {
        this.topics = new EnumMap<>(TopicType.class);
        for (Map.Entry<String, String> entry : topics.entrySet()) {
            try {
                this.topics.put(TopicType.valueOf(entry.getKey()), entry.getValue());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid topic name {}", entry.getKey(), e);
            }
        }
    }
}

