package ru.practicum.model.mapper;

import ru.practicum.dto.HitCreateDto;
import ru.practicum.model.Stat;

public class StatDtoMapper {
    public static Stat mapToModel(HitCreateDto createDto) {
        return Stat.builder()
                .app(createDto.app())
                .uri(createDto.uri())
                .ip(createDto.ip())
                .timestamp(createDto.timestamp())
                .build();
    }
}
