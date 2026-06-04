package ru.practicum;

import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Универсальный сериализатор для Avro-объектов, реализующих {@link SpecificRecordBase}.
 * Использует бинарное кодирование через {@link BinaryEncoder} для преобразования Avro-объектов в байтовые массивы.
 * Поддерживает сериализацию любых объектов, генерируемых Avro-компилятором.
 *
 * <p>Особенности реализации:
 * <ul>
 *   <li>Использует пул энкодеров через {@link EncoderFactory} для оптимизации производительности</li>
 *   <li>Автоматически управляет ресурсами с помощью try-with-resources</li>
 *   <li>Генерирует информативные исключения с контекстом топика</li>
 * </ul>
 *
 * @see Serializer
 * @see SpecificRecordBase
 * @see BinaryEncoder
 * @see EncoderFactory
 */
public class GeneralAvroSerializer implements Serializer<SpecificRecordBase> {
    private final EncoderFactory encoderFactory = EncoderFactory.get();
    private BinaryEncoder encoder;

    /**
     * Сериализует Avro-объект в байтовый массив для отправки в Kafka.
     * <p>
     * Процесс сериализации:
     * <ol>
     *   <li>Создается временный буфер в памяти для записи данных</li>
     *   <li>Инициализируется DatumWriter на основе схемы Avro-объекта</li>
     *   <li>Данные кодируются в бинарный формат с помощью BinaryEncoder</li>
     *   <li>Буфер сбрасывается для завершения записи</li>
     * </ol>
     *
     * @param topic название топика Kafka, для которого выполняется сериализация
     * @param data  Avro-объект для сериализации, может быть null
     * @return байтовый массив с сериализованными данными или null, если data равен null
     * @throws SerializationException если произошла ошибка ввода-вывода при сериализации
     * @see SpecificDatumWriter
     * @see BinaryEncoder
     */
    @Override
    public byte[] serialize(String topic, SpecificRecordBase data) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            if (data != null) {
                DatumWriter<SpecificRecordBase> writer = new SpecificDatumWriter<>(data.getSchema());
                encoder = encoderFactory.binaryEncoder(out, encoder);
                writer.write(data, encoder);
                encoder.flush();
            }
            return out.toByteArray();
        } catch (IOException ex) {
            throw new SerializationException("Ошибка сериализации данных для топика [" + topic + "]", ex);
        }
    }
}
