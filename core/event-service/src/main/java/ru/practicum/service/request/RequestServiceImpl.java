package ru.practicum.service.request;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.client.user.UserFeignClient;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.exception.AlreadyExistsException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.model.EventState;
import ru.practicum.model.RequestStatus;
import ru.practicum.model.event.Event;
import ru.practicum.model.request.Request;
import ru.practicum.model.request.mapper.RequestMapper;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.RequestRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final UserFeignClient userFeignClient;

    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;

    private final RequestMapper requestMapper;

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        UserDto userDto = userFeignClient.getUserById(userId);

        return requestRepository.findAllByRequesterId(userDto.id()).stream()
                .map(requestMapper::toDto)
                .toList();
    }

    @Override
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        UserDto userDto = userFeignClient.getUserById(userId);
        Event event = getEventById(eventId);

        validateCreation(userDto, event);

        return requestMapper.toDto(requestRepository.save(Request.builder()
                .createdOn(LocalDateTime.now())
                .event(event)
                .requesterId(userDto.id())
                .status(event.getRequestModeration() && event.getParticipantLimit() > 0 ? RequestStatus.PENDING : RequestStatus.CONFIRMED)
                .build()));
    }

    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        UserDto userDto = userFeignClient.getUserById(userId);
        Request request = getRequestById(requestId);

        if (!userDto.id().equals(request.getRequesterId())) {
            throw new ValidationException("Пользователь id=" + userDto.id() + " не может отменить заявку id=" + request.getId());
        }

        if (RequestStatus.CANCELED.equals(request.getStatus()) || RequestStatus.REJECTED.equals(request.getStatus())) {
            throw new ValidationException("Статус заявки " + request.getStatus() + " не позволяет выполнить отмену");
        }

        request.setStatus(RequestStatus.CANCELED);

        return requestMapper.toDto(requestRepository.save(request));
    }

    private Request getRequestById(Long requestId) {
        return requestRepository.findById(requestId).orElseThrow(() -> new NotFoundException("Request " + requestId + " not found!"));
    }

    private Event getEventById(Long eventId) {
        return eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Event " + eventId + " not found!"));
    }

    private void validateCreation(UserDto userDto, Event event) {
        if (userDto.id().equals(event.getInitiatorId())) {
            throw new AlreadyExistsException("Пользователя " + userDto.id() + " не может добавить запрос на участие в своем событии " + event.getId());
        }

        if (!EventState.PUBLISHED.equals(event.getState())) {
            throw new AlreadyExistsException("Нельзя участвовать в неопубликованном событии");
        }

        if (requestRepository.findAllByRequesterIdAndEventId(userDto.id(), event.getId()).isPresent()) {
            throw new AlreadyExistsException("Для пользователя " + userDto.id() + " уже существует запрос на участие в событие " + event.getId());
        }

        if (event.getParticipantLimit() != 0 && requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED) >= event.getParticipantLimit()) {
            throw new AlreadyExistsException("Достигнут лимит запросов на участие " + event.getId());
        }
    }
}
