package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.annotation.LogAllMethods;
import ru.practicum.client.RequestClient;
import ru.practicum.service.RequestService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/client/requests")
@RequiredArgsConstructor
@LogAllMethods
public class ClientRequestController implements RequestClient {
    private final RequestService requestService;

    @Override
    @GetMapping("/{eventId}")
    public ResponseEntity<Long> getConfirmedRequests(@PathVariable Long eventId) {
        Long confirmedRequests = requestService.getConfirmedRequests(eventId);
        return ResponseEntity.ok(confirmedRequests);
    }

    @Override
    @GetMapping
    public ResponseEntity<Map<Long, Long>> getConfirmedRequestsForEvents(
            @RequestParam List<Long> eventIds) {
        Map<Long, Long> confirmedRequests = requestService.getConfirmedRequestsForEvents(eventIds);
        return ResponseEntity.ok(confirmedRequests);
    }

    @Override
    @GetMapping("/{eventId}")
    public ResponseEntity<Boolean> hasConfirmedRequestsForEventAndUser(
            @PathVariable Long eventId,
            @RequestHeader("X-EWM-USER-ID")
            Long userId) {
        return ResponseEntity.ok(requestService.hasConfirmedRequestsForEventAndUser(eventId, userId));
    }
}