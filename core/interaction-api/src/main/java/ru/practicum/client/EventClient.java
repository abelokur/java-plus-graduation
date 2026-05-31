package ru.practicum.client;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.event.EventFullDto;

public interface EventClient {
    @GetMapping("/{id}")
    EventFullDto getEventById(@PathVariable Long id);

    @GetMapping
    EventFullDto getEventByIdAndInitiatorId(
            @RequestParam Long eventId,
            @RequestParam Long initiatorId);
}