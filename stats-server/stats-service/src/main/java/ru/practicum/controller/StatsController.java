package ru.practicum.controller;

import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.HitCreateDto;
import ru.practicum.dto.RequestStatsDto;
import ru.practicum.dto.ResponseStatsDto;
import ru.practicum.service.StatsService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Validated
@Slf4j
public class StatsController {
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private final StatsService statsService;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public void saveHit(
            @RequestBody @Valid HitCreateDto createDto
    ) {
        log.info("Запрос на сохранение информации о запросе к эндпоинту с данными: {}", createDto);
        statsService.saveHit(createDto);
    }

    @GetMapping("/stats")
    @ResponseStatus(HttpStatus.OK)
    public List<ResponseStatsDto> getStats(
            @RequestParam(name = "start")
            @DateTimeFormat(pattern = DATE_TIME_PATTERN)
            LocalDateTime start,

            @RequestParam(name = "end")
            @DateTimeFormat(pattern = DATE_TIME_PATTERN)
            LocalDateTime end,

            @RequestParam(name = "uris", required = false)
            List<String> uris,

            @RequestParam(name = "unique", defaultValue = "false")
            Boolean unique
    ) {
        log.info("Запрос на получение статистики по посещениям с данными: start = {}, end = {}, uris = {}, unique = {}",
                start, end, uris, unique);
        if (start != null && end != null && !end.isAfter(start)) {
            throw new ValidationException("Дата конца диапазона должна быть позже даты начала");
        }
        return statsService.getStats(new RequestStatsDto(start, end, uris, unique));
    }
}
