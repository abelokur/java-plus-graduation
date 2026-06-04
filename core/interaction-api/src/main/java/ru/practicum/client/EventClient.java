package ru.practicum.client;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventFullDto;

import java.util.Map;

public interface EventClient {
    @GetMapping("/{id}")
    ResponseEntity<EventFullDto> getEventById(@PathVariable Long id);

    @GetMapping
    ResponseEntity<EventFullDto> getEventByIdAndInitiatorId(
            @RequestParam Long eventId,
            @RequestParam Long initiatorId);

    @PutMapping("/confirmed-requests")
    ResponseEntity<Void> updateEventsConfirmedRequests(@RequestBody Map<Long, Long> confirmedRequests);
}