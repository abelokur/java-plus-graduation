package ru.practicum.service.event;

import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.StatsClient;
import ru.practicum.dto.RequestStatsDto;
import ru.practicum.dto.ResponseStatsDto;
import ru.practicum.dto.event.*;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.dto.request.RequestDtoMapper;
import ru.practicum.exception.ConditionsNotMetException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.model.category.Category;
import ru.practicum.model.event.Event;
import ru.practicum.model.event.EventSort;
import ru.practicum.model.event.State;
import ru.practicum.model.event.mapper.EventMapper;
import ru.practicum.model.request.Request;
import ru.practicum.model.request.RequestStatus;
import ru.practicum.model.user.User;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.RequestRepository;
import ru.practicum.repository.UserRepository;
import ru.practicum.util.OffsetBasedPageable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private static final long MIN_HOURS_BEFORE_PUBLICATION_FOR_ADMIN = 1;
    private static final long MIN_HOURS_BEFORE_UPDATE_FOR_USER = 2;
    private static final LocalDateTime START_DATE_FOR_STAT_REQUEST = LocalDateTime.now().minusYears(1);

    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final StatsClient statsClient;
    private final EventMapper eventMapper;

    @Override
    public List<EventFullDto> findAllAdmin(AdminEventParam params) {
        int from = params.from();
        int size = params.size();
        Pageable pageable = new OffsetBasedPageable(from, size);

        List<Event> events = eventRepository
                .findAll(EventRepository.Predicate.adminFilters(params), pageable).getContent();

        setViewsAndConfirmedRequests(events);

        return events.stream()
                .map(eventMapper::toFullDto)
                .toList();
    }

    @Override
    @Transactional
    public EventFullDto updateAdminEvent(long id, UpdateEventAdminRequest updateRequest) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Событие с id " + id + " не найдена"));

        updateEvent(event, updateRequest);

        setViewsAndConfirmedRequests(event);
        return eventMapper.toFullDto(event);
    }

    @Override
    public List<EventShortDto> findPublicEvents(EventPublicParam params) {
        int from = params.from();
        int size = params.size();
        Sort defaultSort = Sort.by("eventDate");
        Pageable pageable = new OffsetBasedPageable(from, size, defaultSort);
        BooleanBuilder predicate;

        if (params.onlyAvailable() != null && params.onlyAvailable()) {
            List<Long> availableIds = eventRepository.findEventIdsWithAvailableSlots();
            if (availableIds.isEmpty()) {
                return Collections.emptyList();
            }
            predicate = EventRepository.Predicate.publicFilters(params, availableIds);
        } else {
            predicate = EventRepository.Predicate.publicFilters(params);
        }

        List<Event> events = eventRepository
                .findAll(predicate, pageable)
                .getContent();

        setViewsAndConfirmedRequests(events);

        Comparator<EventShortDto> comparator = createEventShortDtoComparator(params.sort());

        return events.stream()
                .map(eventMapper::toShortDto)
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto findPublicEventById(Long eventId) {
        Event event = eventRepository.findByIdAndState(eventId, State.PUBLISHED)
                .orElseThrow(
                        () -> new NotFoundException(String.format("Event with id %d not found", eventId))
                );
        setViewsAndConfirmedRequests(event);
        return eventMapper.toFullDto(event);
    }

    @Override
    public List<EventShortDto> findUserEvents(Long userId, EventPrivateParam params) {
        int from = params.from();
        int size = params.size();
        Sort defaultSort = Sort.by("id").descending();

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(String.format("User with id %d not found", userId));
        }
        Pageable pageable = new OffsetBasedPageable(from, size, defaultSort);
        List<Event> events = eventRepository.findAllByInitiator_Id(userId, pageable);

        setViewsAndConfirmedRequests(events);

        return events.stream()
                .map(eventMapper::toShortDto)
                .toList();
    }

    @Transactional
    @Override
    public EventFullDto createEvent(Long userId, NewEventDto dto) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException(String.format("User with id %d not found", userId)));

        Category category = categoryRepository.findById(dto.category()).orElseThrow(
                () -> new NotFoundException(String.format("Category with id %d not found", dto.category())));

        Event event = eventRepository.save(eventMapper.toEntity(dto, user, category));

        return eventMapper.toFullDto(event);
    }

    @Override
    public EventFullDto findUserEventById(Long eventId, Long userId) {
        Event event = eventRepository.findByIdAndInitiator_Id(eventId, userId).orElseThrow(
                () -> new NotFoundException(String.format("Event with id %d by user %d not found", eventId, userId))
        );
        setViewsAndConfirmedRequests(event);
        return eventMapper.toFullDto(event);
    }

    @Transactional
    @Override
    public EventFullDto updateUserEvent(UpdateEventUserRequestParam requestParam) {
        Event event = eventRepository.findByIdAndInitiator_Id(requestParam.eventId(), requestParam.userId())
                .orElseThrow(
                        () -> new NotFoundException(
                                String.format(
                                        "Event with id %d by user %d not found",
                                        requestParam.eventId(),
                                        requestParam.userId())));

        UpdateEventUserRequest updateRequest = requestParam.request();

        updateEvent(event, updateRequest);
        setViewsAndConfirmedRequests(event);

        return eventMapper.toFullDto(event);
    }

    @Override
    public List<ParticipationRequestDto> findEventRequests(Long eventId, Long userId) {
        List<Request> requests =
                requestRepository.findByEvent_IdAndEvent_Initiator_Id(eventId, userId);
        return RequestDtoMapper.mapRequestToDto(requests);
    }

    @Transactional
    @Override
    public EventRequestStatusUpdateResult updateRequestStatus(EventRequestStatusUpdateRequestParam requestParam) {
        EventRequestStatusUpdateRequest updateRequest = requestParam.updateRequest();
        Event event = eventRepository.findByIdAndInitiator_Id(requestParam.eventId(), requestParam.userId())
                .orElseThrow(
                        () -> new NotFoundException(
                                String.format(
                                        "Event with id %d by user %d not found",
                                        requestParam.eventId(),
                                        requestParam.userId())));

        setConfirmedRequests(event);

        if (event.getParticipantLimit() != 0 &&
            event.getConfirmedRequests() >= event.getParticipantLimit()) {
            throw new ConditionsNotMetException("Достигнут лимит по заявкам на событие - " + event.getId());
        }

        List<Request> requestsToUpdate = requestRepository.findAllById(updateRequest.requestIds());

        requestsToUpdate.forEach(request -> {
            if (!request.getStatus().equals(RequestStatus.PENDING)) {
                throw new ConditionsNotMetException(
                        "Статус можно изменить только у заявок в состоянии ожидания. " +
                        "Текущий статус заявки " + request.getId() + ": " + request.getStatus());
            }
        });

        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            requestsToUpdate.forEach(request -> request.setStatus(RequestStatus.CONFIRMED));
            return new EventRequestStatusUpdateResult(RequestDtoMapper.mapRequestToDto(requestsToUpdate), List.of());
        }

        List<Request> confirmedRequests = new ArrayList<>();
        List<Request> rejectedRequests = new ArrayList<>();
        long availableSlots = event.getParticipantLimit() - event.getConfirmedRequests();

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
                RequestDtoMapper.mapRequestToDto(confirmedRequests),
                RequestDtoMapper.mapRequestToDto(rejectedRequests)
        );
    }

    private Map<Long, Long> getViewsForEvents(List<Long> eventIds) {
        if (eventIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<String> uris = eventIds.stream()
                .map(id -> "/events/" + id)
                .collect(Collectors.toList());

        List<ResponseStatsDto> stats = statsClient.get(createRequestStatsDto(uris, true));

        return stats.stream()
                .collect(Collectors.toMap(
                        stat -> extractEventIdFromUri(stat.uri()),
                        ResponseStatsDto::hits,
                        (existing, replacement) -> existing
                ));
    }

    private Long extractEventIdFromUri(String uri) {
        try {
            return Long.parseLong(uri.replace("/events/", ""));
        } catch (NumberFormatException e) {
            return -1L;
        }
    }

    private Long getViews(Long eventId) {
        List<String> uris = List.of("/events/" + eventId);
        Long views = 0L;
        try {
            views = statsClient.get(createRequestStatsDto(uris, true))
                    .getFirst()
                    .hits();
        } catch (Exception e) {
            return views;
        }
        return views;
    }

    private Long getConfirmedRequests(Long eventId) {
        if (eventId == null) {
            return 0L;
        }
        return requestRepository
                .countByEventAndStatus(eventId, RequestStatus.CONFIRMED);
    }

    private Map<Long, Long> getConfirmedRequestsForEvents(List<Long> eventIds) {
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

    private RequestStatsDto createRequestStatsDto(List<String> uris, boolean unique) {
        return new RequestStatsDto(
                START_DATE_FOR_STAT_REQUEST,
                LocalDateTime.now(),
                uris,
                unique
        );
    }

    private void setViewsAndConfirmedRequests(Event event) {
        setViews(event);
        setConfirmedRequests(event);
    }

    private void setViewsAndConfirmedRequests(List<Event> events) {
        if (events == null || events.isEmpty()) {
            return;
        }

        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .collect(Collectors.toList());

        Map<Long, Long> viewsMap = getViewsForEvents(eventIds);
        Map<Long, Long> confirmedRequestsMap = getConfirmedRequestsForEvents(eventIds);

        events.forEach(event -> {
            event.setViews(viewsMap.getOrDefault(event.getId(), 0L));
            event.setConfirmedRequests(confirmedRequestsMap.getOrDefault(event.getId(), 0L));
        });
    }

    private void setViews(Event event) {
        event.setViews(getViews(event.getId()));
    }

    private void setConfirmedRequests(Event event) {
        event.setConfirmedRequests(getConfirmedRequests(event.getId()));
    }

    private void updateEvent(Event event, UpdateEventUserRequest updateRequest) {
        LocalDateTime now = LocalDateTime.now();

        if (!event.getState().equals(State.PENDING) && !event.getState().equals(State.CANCELED)) {
            throw new ConditionsNotMetException("Ожидается статус PENDING или CANCELED, получен - " + event.getState());
        }

        if (now.plusHours(MIN_HOURS_BEFORE_UPDATE_FOR_USER).isAfter(event.getEventDate())) {
            throw new ConditionsNotMetException("Изменить можно события запланированные " +
                                                "на время не ранее чем через 2 часа от текущего, разница времени - " +
                                                Duration.between(now, event.getEventDate()).toHours());
        }

        Optional.ofNullable(updateRequest.annotation())
                .filter(ann -> !ann.isBlank()).ifPresent(event::setAnnotation);
        Optional.ofNullable(updateRequest.description())
                .filter(desc -> !desc.isBlank()).ifPresent(event::setDescription);
        Optional.ofNullable(updateRequest.eventDate()).ifPresent(event::setEventDate);
        Optional.ofNullable(updateRequest.location()).ifPresent(event::setLocation);
        Optional.ofNullable(updateRequest.paid()).ifPresent(event::setPaid);
        Optional.ofNullable(updateRequest.participantLimit()).ifPresent(event::setParticipantLimit);
        Optional.ofNullable(updateRequest.requestModeration()).ifPresent(event::setRequestModeration);
        Optional.ofNullable(updateRequest.title()).filter(title -> !title.isBlank()).ifPresent(event::setTitle);

        Optional.ofNullable(updateRequest.category()).ifPresent(
                categoryId -> event.setCategory(categoryRepository.findById(categoryId)
                        .orElseThrow(
                                () -> new NotFoundException("Category id " + categoryId + " not found")
                        ))
        );

        if (updateRequest.stateAction() != null) {
            switch (updateRequest.stateAction()) {
                case SEND_TO_REVIEW -> event.setState(State.PENDING);
                case CANCEL_REVIEW -> event.setState(State.CANCELED);
            }
        }
    }

    private void updateEvent(Event event, UpdateEventAdminRequest updateRequest) {
        LocalDateTime now = LocalDateTime.now();

        if (event.getState() != State.PENDING) {
            throw new ConditionsNotMetException(
                    "Событие можно публиковать или отклонить, только если оно в состоянии ожидания публикации. Настоящее состояние: "
                    + event.getState());
        }

        if (now.plusHours(MIN_HOURS_BEFORE_PUBLICATION_FOR_ADMIN).isAfter(event.getEventDate())) {
            throw new ConditionsNotMetException(
                    "Дата начала изменяемого события должна быть не ранее чем за час от даты публикации, разница времени - " +
                    Duration.between(now, event.getEventDate()).toHours());
        }

        Optional.ofNullable(updateRequest.annotation())
                .filter(ann -> !ann.isBlank()).ifPresent(event::setAnnotation);
        Optional.ofNullable(updateRequest.description())
                .filter(desc -> !desc.isBlank()).ifPresent(event::setDescription);
        Optional.ofNullable(updateRequest.eventDate()).ifPresent(event::setEventDate);
        Optional.ofNullable(updateRequest.location()).ifPresent(event::setLocation);
        Optional.ofNullable(updateRequest.paid()).ifPresent(event::setPaid);
        Optional.ofNullable(updateRequest.participantLimit()).ifPresent(event::setParticipantLimit);
        Optional.ofNullable(updateRequest.requestModeration()).ifPresent(event::setRequestModeration);
        Optional.ofNullable(updateRequest.title()).filter(title -> !title.isBlank()).ifPresent(event::setTitle);

        Optional.ofNullable(updateRequest.category()).ifPresent(
                categoryId -> event.setCategory(categoryRepository.findById(categoryId)
                        .orElseThrow(
                                () -> new NotFoundException("Category id " + categoryId + " not found")
                        ))
        );

        if (updateRequest.stateAction() != null) {
            switch (updateRequest.stateAction()) {
                case PUBLISH_EVENT -> {
                    event.setState(State.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                }
                case REJECT_EVENT -> event.setState(State.CANCELED);
            }
        }
    }

    private Comparator<EventShortDto> createEventShortDtoComparator(EventSort sort) {
        Comparator<EventShortDto> comparator;

        if (sort == null) {
            comparator = (a, b) -> 0;
        } else {
            comparator = switch (sort) {
                case VIEWS -> Comparator.comparing(EventShortDto::views).reversed();
                case EVENT_DATE -> Comparator.comparing(EventShortDto::eventDate);
            };
        }
        return comparator;
    }
}
