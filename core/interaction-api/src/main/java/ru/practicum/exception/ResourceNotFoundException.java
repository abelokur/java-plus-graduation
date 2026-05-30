package ru.practicum.exception;

/**
 * Исключение при отсутствии ресурса (HTTP 404).
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}