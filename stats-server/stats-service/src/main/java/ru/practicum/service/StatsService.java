package ru.practicum.service;

import ru.practicum.dto.HitCreateDto;
import ru.practicum.dto.RequestStatsDto;
import ru.practicum.dto.ResponseStatsDto;

import java.util.List;

public interface StatsService {
    void saveHit(HitCreateDto createDto);

    List<ResponseStatsDto> getStats(RequestStatsDto request);
}
