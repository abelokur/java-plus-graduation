package ru.practicum.controller.event;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.client.StatsClient;
import ru.practicum.dto.HitCreateDto;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventPublicParam;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.model.comment.CommentState;
import ru.practicum.model.comment.DateSort;
import ru.practicum.service.comment.CommentService;
import ru.practicum.service.event.EventService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/events")
@Slf4j
@RequiredArgsConstructor
@Validated
public class PublicEventController {
    private final EventService eventService;
    private final StatsClient statsClient;
    private final CommentService commentService;

    @Value("${stats.service.name}")
    private String serviceName;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> findPublicEvents(
            @Valid @ModelAttribute EventPublicParam params,
            HttpServletRequest request) {
        log.info("Public: Method launched (findPublicEvents({}))", params);
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
        log.info("Public: Method launched (findPublicEventById({}))", id);
        EventFullDto event = eventService.findPublicEventById(id);
        saveHit(request);
        return event;
    }

    @GetMapping("/comments")
    public List<CommentDto> getAllComments(@RequestParam(name = "sort", defaultValue = "ASC") DateSort sort) {
        log.info("Public: Get All comments, sort={}", sort);
        return commentService.getCommentsByState(CommentState.APPROVED, sort);
    }

    @GetMapping("/{eventId}/comments")
    public List<CommentDto> getEventComments(@PathVariable(name = "eventId") long eventId,
                                             @RequestParam(name = "sort", defaultValue = "ASC") DateSort sort) {
        log.info("Public: Get event ({}) comments sort={}", eventId, sort);
        return commentService.getCommentsByEvent(eventId, sort);
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
            log.warn("Failed to save statistics for URI: {}", request.getRequestURI(), e);
            // Не бросаем исключение дальше - статистика не должна ломать основной flow
        }
    }
}
