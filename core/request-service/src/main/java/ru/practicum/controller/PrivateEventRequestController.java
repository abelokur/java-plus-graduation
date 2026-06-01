package ru.practicum.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.annotation.LogAllMethods;
import ru.practicum.dto.EventRequestStatusUpdateRequest;
import ru.practicum.dto.EventRequestStatusUpdateRequestParam;
import ru.practicum.dto.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.service.RequestService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events/{eventId}/requests")
@Slf4j
@RequiredArgsConstructor
@Validated
@LogAllMethods
public class PrivateEventRequestController {
    private final RequestService requestService;

    private static final String USER_ID_VALIDATION_MESSAGE = "userId должен быть больше 0";
    private static final String EVENT_ID_VALIDATION_MESSAGE = "eventId должен быть больше 0";

    @GetMapping
    public ResponseEntity<List<ParticipationRequestDto>> findEventRequests(
            @PathVariable
            @Positive(message = USER_ID_VALIDATION_MESSAGE)
            Long userId,

            @PathVariable
            @Positive(message = EVENT_ID_VALIDATION_MESSAGE)
            Long eventId
    ) {
        List<ParticipationRequestDto> requests = requestService.findEventRequests(eventId, userId);
        return ResponseEntity.ok(requests);
    }

    @PatchMapping
    public ResponseEntity<EventRequestStatusUpdateResult> updateRequestStatus(
            @PathVariable
            @Positive(message = USER_ID_VALIDATION_MESSAGE)
            Long userId,

            @PathVariable
            @Positive(message = EVENT_ID_VALIDATION_MESSAGE)
            Long eventId,

            @Valid
            @RequestBody
            EventRequestStatusUpdateRequest updateRequest
    ) {
        EventRequestStatusUpdateRequestParam updateEventRequestParam =
                new EventRequestStatusUpdateRequestParam(userId, eventId, updateRequest);
        EventRequestStatusUpdateResult result = requestService.updateRequestStatus(updateEventRequestParam);
        return ResponseEntity.ok(result);
    }
}