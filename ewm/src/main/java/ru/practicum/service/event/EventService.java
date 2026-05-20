package ru.practicum.service.event;

import ru.practicum.dto.event.AdminEventParam;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventPrivateParam;
import ru.practicum.dto.event.EventPublicParam;
import ru.practicum.dto.event.EventRequestStatusUpdateRequestParam;
import ru.practicum.dto.event.EventRequestStatusUpdateResult;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.event.UpdateEventAdminRequest;
import ru.practicum.dto.event.UpdateEventUserRequestParam;
import ru.practicum.dto.request.ParticipationRequestDto;

import java.util.List;

public interface EventService {
    List<EventFullDto> findAllAdmin(AdminEventParam params);

    EventFullDto updateAdminEvent(long id, UpdateEventAdminRequest event);

    List<EventShortDto> findPublicEvents(EventPublicParam params);

    EventFullDto findPublicEventById(Long eventId);

    List<EventShortDto> findUserEvents(Long userId, EventPrivateParam params);

    EventFullDto createEvent(Long userId, NewEventDto dto);

    EventFullDto findUserEventById(Long eventId, Long userId);

    EventFullDto updateUserEvent(UpdateEventUserRequestParam requestParam);

    List<ParticipationRequestDto> findEventRequests(Long eventId, Long userId);

    EventRequestStatusUpdateResult updateRequestStatus(EventRequestStatusUpdateRequestParam requestParam);
}
