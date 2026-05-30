package ru.practicum.service.request;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.dto.request.RequestDtoMapper;
import ru.practicum.exception.AlreadyExistsException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.model.event.Event;
import ru.practicum.model.event.State;
import ru.practicum.model.request.Request;
import ru.practicum.model.request.RequestStatus;
import ru.practicum.model.user.User;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.RequestRepository;
import ru.practicum.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;

    @Override
    public List<ParticipationRequestDto> getUserRequests(long userId) {
        User user = getUserById(userId);

        return requestRepository.findUserRequests(user.getId()).stream()
                .map(RequestDtoMapper::mapRequestToDto)
                .toList();
    }

    @Override
    public ParticipationRequestDto createRequest(long userId, long eventId) {
        User user = getUserById(userId);
        Event event = getEventById(eventId);

        validateCreation(user, event);

        return RequestDtoMapper.mapRequestToDto(requestRepository.save(Request.builder()
                .createdOn(LocalDateTime.now())
                .event(event)
                .requester(user)
                .status(event.getRequestModeration() && event.getParticipantLimit() > 0 ? RequestStatus.PENDING : RequestStatus.CONFIRMED)
                .build()));
    }

    @Override
    public ParticipationRequestDto cancelRequest(long userId, long requestId) {
        User user = getUserById(userId);
        Request request = getRequestById(requestId);

        if (request.getRequester().getId() != user.getId()) {
            throw new ValidationException("Пользователь id=" + user.getId() + " не может отменить заявку id=" + request.getId());
        }

        if (RequestStatus.CANCELED.equals(request.getStatus()) || RequestStatus.REJECTED.equals(request.getStatus())) {
            throw new ValidationException("Статус заявки " + request.getStatus() + " не позволяет выполнить отмену");
        }

        request.setStatus(RequestStatus.CANCELED);

        return RequestDtoMapper.mapRequestToDto(requestRepository.save(request));
    }

    private User getUserById(long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User " + userId + " not found!"));
    }

    private Request getRequestById(long requestId) {
        return requestRepository.findById(requestId).orElseThrow(() -> new NotFoundException("Request " + requestId + " not found!"));
    }

    private Event getEventById(long eventId) {
        return eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Event " + eventId + " not found!"));
    }

    private void validateCreation(User user, Event event) {
        if (user.getId() == event.getInitiator().getId()) {
            throw new AlreadyExistsException("Пользователя " + user.getId() + " не может добавить запрос на участие в своем событии " + event.getId());
        }

        if (!State.PUBLISHED.equals(event.getState())) {
            throw new AlreadyExistsException("Нельзя участвовать в неопубликованном событии");
        }

        if (requestRepository.findByUserAndEvent(user.getId(), event.getId()).isPresent()) {
            throw new AlreadyExistsException("Для пользователя " + user.getId() + " уже существует запрос на участие в событие " + event.getId());
        }

        if (event.getParticipantLimit() != 0 && requestRepository.countByEventAndStatus(event.getId(), RequestStatus.CONFIRMED) >= event.getParticipantLimit()) {
            throw new AlreadyExistsException("Достигнут лимит запросов на участие " + event.getId());
        }
    }
}
