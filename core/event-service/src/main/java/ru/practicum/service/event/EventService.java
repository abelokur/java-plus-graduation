package ru.practicum.service.event;

import ru.practicum.dto.event.*;

import java.util.List;
import java.util.Map;

public interface EventService {
    List<EventFullDto> findAllAdmin(AdminEventParam params);

    EventFullDto updateAdminEvent(Long eventId, UpdateEventAdminRequest event);

    List<EventShortDto> findPublicEvents(EventPublicParam params);

    EventFullDto findPublicEventById(Long eventId, Long userId);

    List<EventShortDto> findUserEvents(Long userId, EventPrivateParam params);

    EventFullDto createEvent(Long userId, NewEventRequest dto);

    EventFullDto findUserEventById(Long eventId, Long userId);

    EventFullDto updateUserEvent(UpdateEventUserRequestParam requestParam);

    EventFullDto findById(Long eventId);

    EventFullDto findByIdAndInitiatorId(Long eventId, Long initiatorId);

    void updateConfirmedRequests(Map<Long, Long> eventConfirmedRequests);

    List<EventShortDto> findUserRecommendations(Long userId, Integer size);

    void addLikeToEvent(Long eventId, Long userId);
}
