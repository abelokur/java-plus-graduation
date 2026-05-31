package ru.practicum.service;

import ru.practicum.dto.EventRequestStatusUpdateRequestParam;
import ru.practicum.dto.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;

import java.util.List;
import java.util.Map;

public interface RequestService {
    List<ParticipationRequestDto> getUserRequests(Long userId);

    ParticipationRequestDto createRequest(Long userId, Long eventId);

    ParticipationRequestDto cancelRequest(Long userId, Long requestId);

    List<ParticipationRequestDto> findEventRequests(Long eventId, Long userId);

    EventRequestStatusUpdateResult updateRequestStatus(EventRequestStatusUpdateRequestParam requestParam);

    Long getConfirmedRequests(Long eventId);

    Map<Long, Long> getConfirmedRequestsForEvents(List<Long> eventIds);
}
