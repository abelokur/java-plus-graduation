package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
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
    public Long getConfirmedRequests(@PathVariable Long eventId) {
        return requestService.getConfirmedRequests(eventId);
    }

    @Override
    @GetMapping
    public Map<Long, Long> getConfirmedRequestsForEvents(
            @RequestParam List<Long> eventIds) {
        return requestService.getConfirmedRequestsForEvents(eventIds);
    }
}
