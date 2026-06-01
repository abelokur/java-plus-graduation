package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.annotation.LogAllMethods;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.service.RequestService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/requests")
@Slf4j
@RequiredArgsConstructor
@LogAllMethods
public class PrivateRequestController {
    private final RequestService requestService;

    @GetMapping
    public ResponseEntity<List<ParticipationRequestDto>> getUserRequests(@PathVariable(name = "userId") Long userId) {
        List<ParticipationRequestDto> requests = requestService.getUserRequests(userId);
        return ResponseEntity.ok(requests);
    }

    @PostMapping
    public ResponseEntity<ParticipationRequestDto> createRequest(
            @PathVariable(name = "userId") Long userId,
            @RequestParam(name = "eventId") Long eventId) {
        ParticipationRequestDto createdRequest = requestService.createRequest(userId, eventId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRequest);
    }

    @PatchMapping("/{requestId}/cancel")
    public ResponseEntity<ParticipationRequestDto> cancelRequest(
            @PathVariable(name = "userId") Long userId,
            @PathVariable(name = "requestId") Long requestId) {
        ParticipationRequestDto cancelledRequest = requestService.cancelRequest(userId, requestId);
        return ResponseEntity.ok(cancelledRequest);
    }
}