package ru.practicum.exception;

/**
 * Исключение при временной недоступности сервиса (HTTP 503).
 */
public class ServiceTemporaryUnavailableException extends RuntimeException {
    public ServiceTemporaryUnavailableException(String message) {
        super(message);
    }
}