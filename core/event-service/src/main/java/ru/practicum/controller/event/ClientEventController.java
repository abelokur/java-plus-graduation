package ru.practicum.controller.event;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.annotation.LogAllMethods;
import ru.practicum.client.EventClient;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.service.event.EventService;

import java.util.Map;

@RestController
@RequestMapping("/client/events")
@RequiredArgsConstructor
@LogAllMethods
public class ClientEventController implements EventClient {
    private final EventService eventService;

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<EventFullDto> getEventById(@PathVariable Long id) {
        EventFullDto event = eventService.findById(id);
        return ResponseEntity.ok(event);
    }

    @Override
    @GetMapping
    public ResponseEntity<EventFullDto> getEventByIdAndInitiatorId(
            @RequestParam Long eventId,
            @RequestParam Long initiatorId) {
        EventFullDto event = eventService.findByIdAndInitiatorId(eventId, initiatorId);
        return ResponseEntity.ok(event);
    }

    @Override
    @PutMapping("/confirmed-requests")
    public ResponseEntity<Void> updateEventsConfirmedRequests(@RequestBody Map<Long, Long> confirmedRequests) {
        eventService.updateConfirmedRequests(confirmedRequests);
        return ResponseEntity.ok().build();
    }
}