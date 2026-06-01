package ru.practicum.controller.event;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.annotation.LogAllMethods;
import ru.practicum.dto.event.*;
import ru.practicum.service.event.EventService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events")
@Slf4j
@RequiredArgsConstructor
@Validated
@LogAllMethods
public class PrivateEventController {
    private final EventService eventService;
    private static final String USER_ID_VALIDATION_MESSAGE = "userId должен быть больше 0";
    private static final String EVENT_ID_VALIDATION_MESSAGE = "eventId должен быть больше 0";

    @GetMapping
    public ResponseEntity<List<EventShortDto>> findUserEvents(
            @PathVariable
            @Positive(message = USER_ID_VALIDATION_MESSAGE)
            Long userId,

            @Valid
            @ModelAttribute
            EventPrivateParam params
    ) {
        List<EventShortDto> events = eventService.findUserEvents(userId, params);
        return ResponseEntity.ok(events);
    }

    @PostMapping
    public ResponseEntity<EventFullDto> createEvent(
            @PathVariable
            @Positive(message = USER_ID_VALIDATION_MESSAGE)
            Long userId,

            @Valid
            @RequestBody
            NewEventRequest dto
    ) {
        EventFullDto createdEvent = eventService.createEvent(userId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventFullDto> findUserEventById(
            @PathVariable
            @Positive(message = USER_ID_VALIDATION_MESSAGE)
            Long userId,

            @PathVariable
            @Positive(message = EVENT_ID_VALIDATION_MESSAGE)
            Long eventId
    ) {
        EventFullDto event = eventService.findUserEventById(eventId, userId);
        return ResponseEntity.ok(event);
    }

    @PatchMapping("/{eventId}")
    public ResponseEntity<EventFullDto> updateUserEvent(
            @PathVariable
            @Positive(message = USER_ID_VALIDATION_MESSAGE)
            Long userId,

            @PathVariable
            @Positive(message = EVENT_ID_VALIDATION_MESSAGE)
            Long eventId,

            @Valid
            @RequestBody
            UpdateEventUserRequest updateRequest
    ) {
        UpdateEventUserRequestParam updateEventUserRequestParam =
                new UpdateEventUserRequestParam(userId, eventId, updateRequest);
        EventFullDto updatedEvent = eventService.updateUserEvent(updateEventUserRequestParam);
        return ResponseEntity.ok(updatedEvent);
    }
}