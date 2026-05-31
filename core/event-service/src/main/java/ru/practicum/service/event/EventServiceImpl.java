package ru.practicum.service.event;

import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.RequestClient;
import ru.practicum.client.StatsClient;
import ru.practicum.client.UserClient;
import ru.practicum.dto.RequestStatsDto;
import ru.practicum.dto.ResponseStatsDto;
import ru.practicum.dto.event.*;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.exception.ConditionsNotMetException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.model.EventSort;
import ru.practicum.model.EventState;
import ru.practicum.model.category.Category;
import ru.practicum.model.event.Event;
import ru.practicum.model.event.mapper.EventMapper;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.util.OffsetBasedPageable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private static final Long MIN_HOURS_BEFORE_PUBLICATION_FOR_ADMIN = 1L;
    private static final Long MIN_HOURS_BEFORE_UPDATE_FOR_USER = 2L;
    private static final LocalDateTime START_DATE_FOR_STAT_REQUEST = LocalDateTime.now().minusYears(1);

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;

    private final StatsClient statsClient;
    private final UserClient userClient;
    private final RequestClient requestClient;

    private final EventMapper eventMapper;

    @Override
    public List<EventFullDto> findAllAdmin(AdminEventParam params) {
        int from = params.from();
        int size = params.size();
        Pageable pageable = new OffsetBasedPageable(from, size);

        List<Event> events = eventRepository
                .findAll(EventRepository.Predicate.adminFilters(params), pageable)
                .getContent();

        setViews(events); // устанавливаем только количество просмотров, количество подтвержденных запросов берем из БД

        Set<Long> initiatorIds = events.stream()
                .map(Event::getInitiatorId)
                .collect(Collectors.toSet());

        Map<Long, UserShortDto> initiators = getUsers(initiatorIds);

        return events.stream()
                .map(event -> eventMapper.toFullDto(event, initiators.get(event.getInitiatorId())))
                .toList();
    }

    @Override
    @Transactional
    public EventFullDto updateAdminEvent(Long eventId, UpdateEventAdminRequest updateRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id " + eventId + " не найдена"));

        updateEvent(event, updateRequest);

        setViewsAndConfirmedRequests(event); // обновляем так же количество подтвержденных запросов

        return eventMapper.toFullDto(event, getUser(event.getInitiatorId()));
    }

    @Override
    public List<EventShortDto> findPublicEvents(EventPublicParam params) {
        int from = params.from();
        int size = params.size();
        Sort defaultSort = Sort.by("eventDate");
        Pageable pageable = new OffsetBasedPageable(from, size, defaultSort);
        BooleanBuilder predicate = EventRepository.Predicate.publicFilters(params);

        List<Event> events = eventRepository
                .findAll(predicate, pageable)
                .getContent();

        Set<Long> initiatorIds = events.stream()
                .map(Event::getInitiatorId)
                .collect(Collectors.toSet());

        Map<Long, UserShortDto> initiators = getUsers(initiatorIds);

        setViews(events); // устанавливаем только количество просмотров, количество подтвержденных запросов берем из БД

        Comparator<EventShortDto> comparator = createEventShortDtoComparator(params.sort());

        return events.stream()
                .map(event -> eventMapper.toShortDto(event, initiators.get(event.getInitiatorId())))
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto findPublicEventById(Long eventId) {
        Event event = eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(
                        () -> new NotFoundException(String.format("Event with id %d not found", eventId))
                );

        setViews(event); // устанавливаем только количество просмотров, количество подтвержденных запросов берем из БД

        return eventMapper.toFullDto(event, getUser(event.getInitiatorId()));
    }

    @Override
    public List<EventShortDto> findUserEvents(Long userId, EventPrivateParam params) {
        int from = params.from();
        int size = params.size();
        Sort defaultSort = Sort.by("id").descending();

        UserShortDto initiator = getUser(userId);

        Pageable pageable = new OffsetBasedPageable(from, size, defaultSort);
        List<Event> events = eventRepository.findAllByInitiatorId(userId, pageable);

        setViews(events); // устанавливаем только количество просмотров, количество подтвержденных запросов берем из БД

        return events.stream()
                .map(event -> eventMapper.toShortDto(event, initiator))
                .toList();
    }

    @Transactional
    @Override
    public EventFullDto createEvent(Long userId, NewEventRequest dto) {
        UserShortDto initiator = getUser(userId);

        Category category = categoryRepository.findById(dto.category()).orElseThrow(
                () -> new NotFoundException(String.format("Category with id %d not found", dto.category())));

        Event event = eventRepository.save(eventMapper.toEntity(dto, initiator.id(), category));

        return eventMapper.toFullDto(event, initiator);
    }

    @Override
    public EventFullDto findUserEventById(Long eventId, Long userId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId).orElseThrow(
                () -> new NotFoundException(String.format("Event with id %d by user %d not found", eventId, userId))
        );

        setViews(event);

        return eventMapper.toFullDto(event, getUser(userId));
    }

    @Transactional
    @Override
    public EventFullDto updateUserEvent(UpdateEventUserRequestParam requestParam) {
        Event event = eventRepository.findByIdAndInitiatorId(requestParam.eventId(), requestParam.userId())
                .orElseThrow(
                        () -> new NotFoundException(
                                String.format(
                                        "Event with id %d by user %d not found",
                                        requestParam.eventId(),
                                        requestParam.userId())));

        UpdateEventUserRequest updateRequest = requestParam.request();

        updateEvent(event, updateRequest);

        setViewsAndConfirmedRequests(event); // обновляем так же количество подтвержденных запросов

        return eventMapper.toFullDto(event, getUser(event.getInitiatorId()));
    }

    @Override
    public EventFullDto findById(Long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException(String.format("Event with id %d not found", eventId))
        );
        return eventMapper.toFullDto(event, getUser(event.getInitiatorId()));
    }

    @Override
    public EventFullDto findByIdAndInitiatorId(Long eventId, Long initiatorId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, initiatorId)
                .orElseThrow(
                        () -> new NotFoundException(
                                String.format(
                                        "Event with id %d by user %d not found",
                                        eventId,
                                        initiatorId)));
        return eventMapper.toFullDto(event, getUser(initiatorId));
    }

    @Override
    @Transactional
    public void updateConfirmedRequests(Map<Long, Long> eventConfirmedRequests) {
        List<Event> events = eventRepository.findAllById(eventConfirmedRequests.keySet());
        events.forEach(event -> {
            Long confirmedCount = eventConfirmedRequests.get(event.getId());
            if (confirmedCount != null) {
                event.setConfirmedRequests(confirmedCount);
            }
        });
        eventRepository.saveAll(events);
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

    private void setViews(Event event) {
        event.setViews(getViews(event.getId()));
    }

    private void setViews(List<Event> events) {
        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .toList();
        Map<Long, Long> views = getViewsForEvents(eventIds);
        events.forEach(event -> event.setViews(views.get(event.getId())));
    }

    private void setConfirmedRequests(Event event) {
        event.setConfirmedRequests(requestClient.getConfirmedRequests(event.getId()));
    }

    private void updateEvent(Event event, BaseUpdateEventRequest updateRequest) {
        LocalDateTime now = LocalDateTime.now();

        if (updateRequest.getUpdateType() == BaseUpdateEventRequest.UpdateType.USER) {
            if (!event.getState().equals(EventState.PENDING) && !event.getState().equals(EventState.CANCELED)) {
                throw new ConditionsNotMetException("Ожидается статус PENDING или CANCELED, получен - "
                                                    + event.getState());
            }

            if (now.plusHours(MIN_HOURS_BEFORE_UPDATE_FOR_USER).isAfter(event.getEventDate())) {
                throw new ConditionsNotMetException("Изменить можно события запланированные " +
                                                    "на время не ранее чем через 2 часа от текущего, разница времени - " +
                                                    Duration.between(now, event.getEventDate()).toHours());
            }

            generalUpdateEvent(event, updateRequest);

            if (updateRequest.getStateAction() != null) {
                switch (updateRequest.getStateAction()) {
                    case SEND_TO_REVIEW -> event.setState(EventState.PENDING);
                    case CANCEL_REVIEW -> event.setState(EventState.CANCELED);
                }
            }
        }

        if (updateRequest.getUpdateType() == BaseUpdateEventRequest.UpdateType.ADMIN) {
            if (event.getState() != EventState.PENDING) {
                throw new ConditionsNotMetException(
                        "Событие можно публиковать или отклонить, только если оно в состоянии ожидания публикации. Настоящее состояние: "
                        + event.getState());
            }

            if (now.plusHours(MIN_HOURS_BEFORE_PUBLICATION_FOR_ADMIN).isAfter(event.getEventDate())) {
                throw new ConditionsNotMetException(
                        "Дата начала изменяемого события должна быть не ранее чем за час от даты публикации, разница времени - " +
                        Duration.between(now, event.getEventDate()).toHours());
            }

            generalUpdateEvent(event, updateRequest);

            if (updateRequest.getStateAction() != null) {
                switch (updateRequest.getStateAction()) {
                    case PUBLISH_EVENT -> {
                        event.setState(EventState.PUBLISHED);
                        event.setPublishedOn(LocalDateTime.now());
                    }
                    case REJECT_EVENT -> event.setState(EventState.CANCELED);
                }
            }
        }
    }

    private void generalUpdateEvent(Event event, BaseUpdateEventRequest updateRequest) {
        Optional.ofNullable(updateRequest.getAnnotation())
                .filter(ann -> !ann.isBlank()).ifPresent(event::setAnnotation);
        Optional.ofNullable(updateRequest.getDescription())
                .filter(desc -> !desc.isBlank()).ifPresent(event::setDescription);
        Optional.ofNullable(updateRequest.getEventDate()).ifPresent(event::setEventDate);
        Optional.ofNullable(updateRequest.getLocation()).ifPresent(event::setLocation);
        Optional.ofNullable(updateRequest.getPaid()).ifPresent(event::setPaid);
        Optional.ofNullable(updateRequest.getParticipantLimit()).ifPresent(event::setParticipantLimit);
        Optional.ofNullable(updateRequest.getRequestModeration()).ifPresent(event::setRequestModeration);
        Optional.ofNullable(updateRequest.getTitle()).filter(title -> !title.isBlank()).ifPresent(event::setTitle);

        Optional.ofNullable(updateRequest.getCategory()).ifPresent(
                categoryId -> event.setCategory(categoryRepository.findById(categoryId)
                        .orElseThrow(
                                () -> new NotFoundException("Category id " + categoryId + " not found")
                        ))
        );
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

    private Map<Long, UserShortDto> getUsers(Set<Long> usersIds) {
        return userClient.getUsersByIds(usersIds).stream()
                .collect(Collectors.toMap(UserDto::id, UserDto::toShortDto));
    }

    private UserShortDto getUser(Long userId) {
        return userClient.getUserById(userId).toShortDto();
    }
}
