package ru.practicum.dto.event;

public record EventRequestStatusUpdateRequestParam(
        Long userId,
        Long eventId,
        EventRequestStatusUpdateRequest updateRequest
) {
}
