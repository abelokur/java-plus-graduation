package ru.practicum.client;

import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventFullDto;

import java.util.Map;

public interface EventClient {
    @GetMapping("/{id}")
    EventFullDto getEventById(@PathVariable Long id);

    @GetMapping
    EventFullDto getEventByIdAndInitiatorId(
            @RequestParam Long eventId,
            @RequestParam Long initiatorId);

    @PutMapping("/confirmed-requests")
    void updateEventsConfirmedRequests(@RequestBody Map<Long, Long> confirmedRequests);
}