package ru.practicum.client;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

public interface RequestClient {
    @GetMapping("/{eventId}")
    Long getConfirmedRequests(@PathVariable Long eventId);

    @GetMapping
    Map<Long, Long> getConfirmedRequestsForEvents(
            @RequestParam List<Long> eventIds);
}