package ru.practicum.service.event;

import com.querydsl.core.BooleanBuilder;
import ewm.client.stats.CollectorClient;
import ewm.client.stats.RecommendationsClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.RequestClient;
import ru.practicum.client.UserClient;
import ru.practicum.dto.event.*;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.exception.ConditionsNotMetException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ServiceTemporaryUnavailableException;
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
@Slf4j
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private static final Long MIN_HOURS_BEFORE_PUBLICATION_FOR_ADMIN = 1L;
    private static final Long MIN_HOURS_BEFORE_UPDATE_FOR_USER = 2L;
    private static final LocalDateTime START_DATE_FOR_STAT_REQUEST = LocalDateTime.now().minusYears(1);

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;

    private final UserClient userClient;
    private final RequestClient requestClient;
    private final RecommendationsClient recommendationsClient;
    private final CollectorClient collectorClient;

    private final EventMapper eventMapper;

    @Override
    public List<EventFullDto> findAllAdmin(AdminEventParam params) {
        int from = params.from();
        int size = params.size();
        Pageable pageable = new OffsetBasedPageable(from, size);

        List<Event> events = eventRepository
                .findAll(EventRepository.Predicate.adminFilters(params), pageable)
                .getContent();

        setRating(events);

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

        setRatingAndConfirmedRequests(event); // обновляем так же количество подтвержденных запросов

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

        setRating(events); // устанавливаем только рейтинг, количество подтвержденных запросов берем из БД

        Comparator<EventShortDto> comparator = createEventShortDtoComparator(params.sort());

        return events.stream()
                .map(event -> eventMapper.toShortDto(event, initiators.get(event.getInitiatorId())))
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto findPublicEventById(Long eventId, Long userId) {
        Event event = eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(
                        () -> new NotFoundException(String.format("Event with id %d not found", eventId))
                );

        setRating(event); // устанавливаем только рейтинг, количество подтвержденных запросов берем из БД

        try {
            collectorClient.saveView(userId, eventId);
        } catch (Exception e) {
            log.warn("Error by sending view to collector service", e);
        }

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

        setRating(events); // устанавливаем только рейтинг, количество подтвержденных запросов берем из БД

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

        setRating(event);

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

        setRatingAndConfirmedRequests(event); // обновляем так же количество подтвержденных запросов

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

    @Override
    public List<EventShortDto> findUserRecommendations(Long userId, Integer size) {
        UserShortDto user = getUser(userId);

        Map<Long, Double> recommendedEvents;
        try {
            recommendedEvents = recommendationsClient.getRecommendationsForUser(userId, size)
                    .collect(Collectors.toMap(RecommendedEventProto::getEventId, RecommendedEventProto::getScore));
        } catch (Exception e) {
            log.error("Error getting recommendations for user {}", userId, e);
            return Collections.emptyList();
        }

        if (recommendedEvents.isEmpty()) {
            return Collections.emptyList();
        }

        List<Event> events = eventRepository.findAllById(recommendedEvents.keySet());

        Set<Long> initiatorIds = events.stream()
                .map(Event::getInitiatorId)
                .collect(Collectors.toSet());

        Map<Long, UserShortDto> initiators = getUsers(initiatorIds);

        return events.stream().map(e ->  {
            e.setRating(recommendedEvents.get(e.getId()));
            return eventMapper.toShortDto(e, initiators.get(e.getInitiatorId()));
                })
                .sorted(Comparator.comparing(EventShortDto::rating).reversed())
                .toList();
    }

    @Override
    public void addLikeToEvent(Long eventId, Long userId) {
        Boolean hasConfirmedRequests;

        try {
            ResponseEntity<Boolean> response = requestClient.hasConfirmedRequestsForEventAndUser(eventId, userId);
            hasConfirmedRequests = response.getBody();
            if (hasConfirmedRequests == null) {
                throw new ServiceTemporaryUnavailableException("Requests service returned null response");
            }
        } catch (Exception e) {
            log.error("Error getting confirmed requests for event {} by user {}", eventId, userId, e);
            throw new ServiceTemporaryUnavailableException("Requests service is temporary unavailable");
        }

        if (!hasConfirmedRequests) {
            throw new ConditionsNotMetException(String.format("User: %d not participant of event: %d",
                    userId, eventId));
        }

        try {
            collectorClient.saveLike(userId, eventId);
        } catch (Exception e) {
            log.error("Error saving like for event {} by user {}", eventId, userId, e);
            throw new ServiceTemporaryUnavailableException("Collector service is temporary unavailable");
        }
    }

    private void setRating(Event event) {
        try {
            Double rating = recommendationsClient.getInteractionsCount(List.of(event.getId()))
                    .findFirst()
                    .map(RecommendedEventProto::getScore)
                    .orElse(0.0);

            event.setRating(rating);
        } catch (Exception e) {
            log.warn("Error getting rating for event {}", event.getId(), e);
        }
    }

    private void setRating(List<Event> events) {
        try {
            List<Long> eventIds = events.stream().map(Event::getId).toList();

            Map<Long, Double> ratings = recommendationsClient.getInteractionsCount(eventIds)
                    .collect(Collectors.toMap(RecommendedEventProto::getEventId, RecommendedEventProto::getScore));

            events.forEach(event ->
                    event.setRating(ratings.getOrDefault(event.getId(), 0.0)));
        } catch (Exception e) {
            log.warn("Error getting rating for events [{}]", events.size(), e);
        }
    }

    private void setConfirmedRequests(Event event) {
        try {
            ResponseEntity<Long> response = requestClient.getConfirmedRequests(event.getId());
            Long confirmedRequests = response.getBody();
            event.setConfirmedRequests(confirmedRequests != null ? confirmedRequests : 0L);
        } catch (Exception e) {
            log.warn("Error getting confirmed requests for event {}", event.getId(), e);
            event.setConfirmedRequests(0L);
        }
    }

    private void setRatingAndConfirmedRequests(Event event) {
        setRating(event);
        setConfirmedRequests(event);
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
                case RATING -> Comparator.comparing(EventShortDto::rating).reversed();
                case EVENT_DATE -> Comparator.comparing(EventShortDto::eventDate);
            };
        }
        return comparator;
    }

    private Map<Long, UserShortDto> getUsers(Set<Long> usersIds) {
        ResponseEntity<Set<UserDto>> response = userClient.getUsersByIds(usersIds);
        Set<UserDto> users = response.getBody();
        if (users == null) {
            return Collections.emptyMap();
        }
        return users.stream()
                .collect(Collectors.toMap(UserDto::id, UserDto::toShortDto));
    }

    private UserShortDto getUser(Long userId) {
        ResponseEntity<UserDto> response = userClient.getUserById(userId);
        UserDto userDto = response.getBody();
        if (userDto == null) {
            throw new NotFoundException("User with id " + userId + " not found");
        }
        return userDto.toShortDto();
    }
}
