package ru.practicum.dto;

import lombok.Builder;

public record ResponseStatsDto(
        String app,
        String uri,
        Long hits
) {
    @Builder(toBuilder = true)
    public ResponseStatsDto {
    }
}
