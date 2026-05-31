package ru.practicum.dto;

public record EventRequestStatusUpdateRequestParam(
        Long userId,
        Long eventId,
        EventRequestStatusUpdateRequest updateRequest
) {
}
