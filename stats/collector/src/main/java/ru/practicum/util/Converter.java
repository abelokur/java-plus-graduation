package ru.practicum.util;

import java.time.Instant;

/**
 * Утилитарный класс для общих конвертеров различных форматов
 */
public class Converter {
    /**
     * Конвертирует protobuf.Timestamp в Instant.
     *
     * @param timestamp из коллекции protobuf.
     * @return Instant
     */
    public static Instant timestampToInstant(com.google.protobuf.Timestamp timestamp) {
        return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
    }
}
