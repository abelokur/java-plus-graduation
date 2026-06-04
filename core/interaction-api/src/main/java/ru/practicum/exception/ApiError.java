package ru.practicum.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.util.DateTimeFormat.DATE_TIME_PATTERN;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        String status,
        String reason,
        String message,
        @JsonFormat(pattern = DATE_TIME_PATTERN)
        LocalDateTime timestamp,
        List<String> errors
) {
    public ApiError(String status, String reason, String message) {
        this(status, reason, message, LocalDateTime.now(), null);
    }

    public ApiError(String status, String reason, String message, List<String> errors) {
        this(status, reason, message, LocalDateTime.now(), errors);
    }
}