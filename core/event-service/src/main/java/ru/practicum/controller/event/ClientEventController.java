package ru.practicum.controller.event;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.annotation.LogAllMethods;
import ru.practicum.client.EventClient;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.service.event.EventService;

@RestController
@RequestMapping("/client/events")
@RequiredArgsConstructor
@LogAllMethods
public class ClientEventController implements EventClient {
    private final EventService eventService;

    @Override
    @GetMapping("/{id}")
    public EventFullDto getEventById(@PathVariable Long id) {
        return eventService.findById(id);
    }

    @Override
    @GetMapping
    public EventFullDto getEventByIdAndInitiatorId(
            @RequestParam Long eventId,
            @RequestParam Long initiatorId) {
        return eventService.findByIdAndInitiatorId(eventId, initiatorId);
    }
}
