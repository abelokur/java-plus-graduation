package ru.practicum.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.EventClient;
import ru.practicum.client.UserClient;
import ru.practicum.dto.EventRequestStatusUpdateRequest;
import ru.practicum.dto.EventRequestStatusUpdateRequestParam;
import ru.practicum.dto.EventRequestStatusUpdateResult;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.exception.AlreadyExistsException;
import ru.practicum.exception.ConditionsNotMetException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.model.EventState;
import ru.practicum.model.Request;
import ru.practicum.model.RequestStatus;
import ru.practicum.model.mapper.RequestMapper;
import ru.practicum.repository.RequestRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final UserClient userClient;
    private final EventClient eventClient;

    private final RequestRepository requestRepository;

    private final RequestMapper requestMapper;

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        UserDto userDto = userClient.getUserById(userId);

        return requestRepository.findAllByRequesterId(userDto.id()).stream()
                .map(requestMapper::toDto)
                .toList();
    }

    @Override
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        UserDto userDto = userClient.getUserById(userId);
        EventFullDto event = eventClient.getEventById(eventId);

        validateCreation(userDto, event);

        return requestMapper.toDto(requestRepository.save(Request.builder()
                .createdOn(LocalDateTime.now())
                .eventId(event.id())
                .requesterId(userDto.id())
                .status(event.requestModeration() && event.participantLimit() > 0 ?
                        RequestStatus.PENDING :
                        RequestStatus.CONFIRMED)
                .build()));
    }

    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        UserDto userDto = userClient.getUserById(userId);
        Request request = getRequestById(requestId);

        if (!userDto.id().equals(request.getRequesterId())) {
            throw new ValidationException("Пользователь id=" + userDto.id() +
                                          " не может отменить заявку id=" + request.getId());
        }

        if (RequestStatus.CANCELED.equals(request.getStatus()) || RequestStatus.REJECTED.equals(request.getStatus())) {
            throw new ValidationException("Статус заявки " + request.getStatus() + " не позволяет выполнить отмену");
        }

        request.setStatus(RequestStatus.CANCELED);

        return requestMapper.toDto(requestRepository.save(request));
    }

    @Override
    public List<ParticipationRequestDto> findEventRequests(Long eventId, Long userId) {
        EventFullDto event = eventClient.getEventByIdAndInitiatorId(eventId, userId);

        if (event == null) {
            throw new NotFoundException("Событие id=" + eventId + " не найдено");
        }

        if (!event.initiator().id().equals(userId)) {
            throw new ValidationException("Пользователь id=" + userId + " не является организатором события id=" + eventId);
        }

        List<Request> requests =
                requestRepository.findAllByEventId(eventId);

        return requestMapper.toDto(requests);
    }

    @Transactional
    @Override
    public EventRequestStatusUpdateResult updateRequestStatus(EventRequestStatusUpdateRequestParam requestParam) {
        EventRequestStatusUpdateRequest updateRequest = requestParam.updateRequest();
        EventFullDto event = eventClient.getEventByIdAndInitiatorId(requestParam.eventId(), requestParam.userId());

        if (event == null) {
            throw new NotFoundException("Событие id=" + requestParam.eventId() + " не найдено");
        }

        if (!event.initiator().id().equals(requestParam.userId())) {
            throw new ValidationException("Пользователь id=" + requestParam.userId()
                                          + " не является организатором события id=" + requestParam.eventId());
        }

        Long confirmed = getConfirmedRequests(requestParam.eventId());

        if (requestParam.updateRequest().status().equals(RequestStatus.CONFIRMED) &&
            event.participantLimit() != 0 &&
            confirmed >= event.participantLimit()) {
            throw new ConditionsNotMetException("Достигнут лимит по заявкам на событие - " + event.id());
        }

        List<Request> requestsToUpdate = requestRepository.findAllById(updateRequest.requestIds());

        requestsToUpdate.forEach(request -> {
            if (!request.getStatus().equals(RequestStatus.PENDING)) {
                throw new ConditionsNotMetException(
                        "Статус можно изменить только у заявок в состоянии ожидания. " +
                        "Текущий статус заявки " + request.getId() + ": " + request.getStatus());
            }
        });

        if (event.participantLimit() == 0 || !event.requestModeration()) {
            requestsToUpdate.forEach(request -> request.setStatus(RequestStatus.CONFIRMED));

            return new EventRequestStatusUpdateResult(requestMapper.toDto(requestsToUpdate), List.of());
        }

        List<Request> confirmedRequests = new ArrayList<>();
        List<Request> rejectedRequests = new ArrayList<>();

        long availableSlots = event.participantLimit() - event.confirmedRequests();

        for (Request request : requestsToUpdate) {
            if (availableSlots > 0 && updateRequest.status().equals(RequestStatus.CONFIRMED)) {
                request.setStatus(RequestStatus.CONFIRMED);
                confirmedRequests.add(request);
                availableSlots--;
            } else {
                request.setStatus(RequestStatus.REJECTED);
                rejectedRequests.add(request);
            }
        }

        return new EventRequestStatusUpdateResult(
                requestMapper.toDto(confirmedRequests),
                requestMapper.toDto(rejectedRequests)
        );
    }

    @Override
    public Long getConfirmedRequests(Long eventId) {
        if (eventId == null) {
            return 0L;
        }
        return requestRepository
                .countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
    }

    @Override
    public Map<Long, Long> getConfirmedRequestsForEvents(List<Long> eventIds) {
        try {
            return requestRepository.countConfirmedRequestsByEventIds(eventIds).stream()
                    .collect(Collectors.toMap(
                            e -> (Long) e[0],
                            e -> (Long) e[1]
                    ));
        } catch (Exception e) {
            return eventIds.stream().collect(Collectors.toMap(id -> id, id -> 0L));
        }
    }

    private Request getRequestById(Long requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request " + requestId + " not found!"));
    }

    private void validateCreation(UserDto userDto, EventFullDto event) {
        if (userDto.id().equals(event.initiator().id())) {
            throw new AlreadyExistsException("Пользователя " + userDto.id() +
                                             " не может добавить запрос на участие в своем событии " + event.id());
        }

        if (!EventState.PUBLISHED.equals(event.state())) {
            throw new AlreadyExistsException("Нельзя участвовать в неопубликованном событии");
        }

        if (requestRepository.findAllByRequesterIdAndEventId(userDto.id(), event.id()).isPresent()) {
            throw new AlreadyExistsException("Для пользователя " + userDto.id() +
                                             " уже существует запрос на участие в событие " + event.id());
        }

        if (event.participantLimit() != 0 &&
            requestRepository.countByEventIdAndStatus(event.id(), RequestStatus.CONFIRMED) >= event.participantLimit()) {
            throw new AlreadyExistsException("Достигнут лимит запросов на участие " + event.id());
        }
    }
}
