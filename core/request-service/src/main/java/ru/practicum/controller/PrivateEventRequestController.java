package ru.practicum.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
    @ResponseStatus(HttpStatus.OK)
    public List<ParticipationRequestDto> findEventRequests(
            @PathVariable
            @Positive(message = USER_ID_VALIDATION_MESSAGE)
            Long userId,

            @PathVariable
            @Positive(message = EVENT_ID_VALIDATION_MESSAGE)
            Long eventId
    ) {
        return requestService.findEventRequests(eventId, userId);
    }

    @PatchMapping
    @ResponseStatus(HttpStatus.OK)
    public EventRequestStatusUpdateResult updateRequestStatus(
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
        return requestService.updateRequestStatus(updateEventRequestParam);
    }
}
