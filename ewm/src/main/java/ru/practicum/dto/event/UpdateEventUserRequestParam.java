package ru.practicum.dto.event;

public record UpdateEventUserRequestParam(
        Long userId,
        Long eventId,
        UpdateEventUserRequest request
) {
}
