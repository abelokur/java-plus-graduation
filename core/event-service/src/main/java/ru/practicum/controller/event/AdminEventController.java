package ru.practicum.controller.event;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.annotation.LogAllMethods;
import ru.practicum.dto.event.AdminEventParam;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.UpdateEventAdminRequest;
import ru.practicum.service.event.EventService;

import java.util.List;

@RestController
@RequestMapping("/admin/events")
@Slf4j
@RequiredArgsConstructor
@Validated
@LogAllMethods
public class AdminEventController {
    private final EventService eventService;

    @GetMapping
    public ResponseEntity<List<EventFullDto>> findAll(
            @Valid @ModelAttribute AdminEventParam params
    ) {
        List<EventFullDto> events = eventService.findAllAdmin(params);
        return ResponseEntity.ok(events);
    }

    @PatchMapping("/{eventId}")
    public ResponseEntity<EventFullDto> update(
            @PathVariable @Positive Long eventId,
            @Valid @RequestBody UpdateEventAdminRequest event
    ) {
        EventFullDto updatedEvent = eventService.updateAdminEvent(eventId, event);
        return ResponseEntity.ok(updatedEvent);
    }
}