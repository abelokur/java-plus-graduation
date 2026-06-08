package ru.practicum.client;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

public interface RequestClient {
    @GetMapping("/{eventId}")
    ResponseEntity<Long> getConfirmedRequests(@PathVariable Long eventId);

    @GetMapping
    ResponseEntity<Map<Long, Long>> getConfirmedRequestsForEvents(@RequestParam List<Long> eventIds);

    @GetMapping("/{eventId}/confirmed")
    ResponseEntity<Boolean> hasConfirmedRequestsForEventAndUser(
            @PathVariable Long eventId,
            @RequestHeader("X-EWM-USER-ID") Long userId);
}