package ru.practicum.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public record RequestStatsDto(
        LocalDateTime start,
        LocalDateTime end,
        List<String> uris,
        Boolean unique
) {
    @Builder(toBuilder = true)
    public RequestStatsDto {
    }
}
