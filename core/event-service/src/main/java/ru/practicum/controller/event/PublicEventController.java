package ru.practicum.controller.event;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.annotation.LogAllMethods;
import ru.practicum.client.StatsClient;
import ru.practicum.dto.HitCreateDto;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventPublicParam;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.service.event.EventService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/events")
@Slf4j
@RequiredArgsConstructor
@Validated
@LogAllMethods
public class PublicEventController {
    private final EventService eventService;
    private final StatsClient statsClient;

    @Value("${stats.service.name:event-service}")
    private String serviceName;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> findPublicEvents(
            @Valid @ModelAttribute EventPublicParam params,
            HttpServletRequest request) {
        List<EventShortDto> events = eventService.findPublicEvents(params);
        if (!events.isEmpty()) {
            saveHit(request);
        }
        return events;
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto findPublicEventById(
            @Positive(message = "eventId должен быть больше 0")
            @PathVariable
            Long id,
            HttpServletRequest request
    ) {
        EventFullDto event = eventService.findPublicEventById(id);
        saveHit(request);
        return event;
    }

    private void saveHit(HttpServletRequest request) {
        try {
            statsClient.hit(HitCreateDto.builder()
                    .app(serviceName)
                    .uri(request.getRequestURI())
                    .ip(request.getRemoteAddr())
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            // Не бросаем исключение дальше - статистика не должна ломать основной flow
        }
    }
}
