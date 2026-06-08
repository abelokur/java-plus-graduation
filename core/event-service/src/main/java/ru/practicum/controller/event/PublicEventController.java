package ru.practicum.controller.event;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.annotation.LogAllMethods;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventPublicParam;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.service.event.EventService;

import java.util.List;

@RestController
@RequestMapping("/events")
@Slf4j
@RequiredArgsConstructor
@Validated
@LogAllMethods
public class PublicEventController {
    private final EventService eventService;

    @GetMapping
    public ResponseEntity<List<EventShortDto>> findPublicEvents(
            @Valid @ModelAttribute EventPublicParam params) {
        List<EventShortDto> events = eventService.findPublicEvents(params);

        return ResponseEntity.ok(events);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventFullDto> findPublicEventById(
            @Positive(message = "eventId должен быть больше 0")
            @PathVariable
            Long id,
            @RequestHeader("X-EWM-USER-ID")
            Long userId
    ) {
        EventFullDto event = eventService.findPublicEventById(id, userId);
        return ResponseEntity.ok(event);
    }

    @GetMapping("/recommendations")
    public ResponseEntity<List<EventShortDto>> findUserRecommendations(
            @RequestHeader("X-EWM-USER-ID") Long userId,
            @RequestParam(defaultValue = "10")
            @Positive(message = "size должен быть больше 0")
            Integer size
    ) {
        return ResponseEntity.ok(eventService.findUserRecommendations(userId, size));
    }

    @PutMapping("/{id}/like")
    public ResponseEntity<Void> addLikeToEvent(
            @Positive(message = "eventId должен быть больше 0")
            @PathVariable
            Long id,
            @RequestHeader("X-EWM-USER-ID")
            Long userId
    ) {
        eventService.addLikeToEvent(id, userId);
        return ResponseEntity.ok().build();
    }

    /*private void saveHit(HttpServletRequest request) {
        try {
            statsClient.hit(HitCreateDto.builder()
                    .app(serviceName)
                    .uri(request.getRequestURI())
                    .ip(request.getRemoteAddr())
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            log.warn("Failed to save hit for URI: {}", request.getRequestURI(), e);
        }
    }*/
}