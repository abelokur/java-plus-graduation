package ru.practicum.service.event;

import ru.practicum.dto.event.*;

import java.util.List;

public interface EventService {
    List<EventFullDto> findAllAdmin(AdminEventParam params);

    EventFullDto updateAdminEvent(Long eventId, UpdateEventAdminRequest event);

    List<EventShortDto> findPublicEvents(EventPublicParam params);

    EventFullDto findPublicEventById(Long eventId);

    List<EventShortDto> findUserEvents(Long userId, EventPrivateParam params);

    EventFullDto createEvent(Long userId, NewEventRequest dto);

    EventFullDto findUserEventById(Long eventId, Long userId);

    EventFullDto updateUserEvent(UpdateEventUserRequestParam requestParam);

    EventFullDto findById(Long eventId);

    EventFullDto findByIdAndInitiatorId(Long eventId, Long initiatorId);
}
