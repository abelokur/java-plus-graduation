package ru.practicum.service.compilation;

import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.user.UserFeignClient;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationRequest;
import ru.practicum.dto.compilation.UpdateCompilationRequest;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.model.compilation.Compilation;
import ru.practicum.model.compilation.CompilationEvent;
import ru.practicum.model.compilation.EventCompilationId;
import ru.practicum.model.compilation.mapper.CompilationMapper;
import ru.practicum.model.event.Event;
import ru.practicum.model.event.mapper.EventMapper;
import ru.practicum.repository.CompilationEventRepository;
import ru.practicum.repository.CompilationRepository;
import ru.practicum.repository.EventRepository;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final EventRepository eventRepository;
    private final CompilationRepository compilationRepository;
    private final CompilationEventRepository compilationEventRepository;

    private final EventMapper eventMapper;
    private final CompilationMapper compilationMapper;

    private final UserFeignClient userFeignClient;

    @Override
    @Transactional
    @CacheEvict(cacheNames = "compilations", allEntries = true)
    public CompilationDto createCompilation(NewCompilationRequest compilationDto) {
        Set<Event> events = getEvents(compilationDto.eventIds());

        final Compilation compilation = compilationRepository.save(Compilation.builder()
                .title(compilationDto.title())
                .pinned(compilationDto.pinned() != null ? compilationDto.pinned() : false)
                .build());

        saveCompilationEvents(compilation, events);

        return findCompilationById(compilation.getId());
    }

    @Override
    @CacheEvict(cacheNames = "compilations", allEntries = true)
    public void deleteCompilation(Long compilationId) {
        Compilation compilation = getCompilationById(compilationId);
        compilationRepository.delete(compilation);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "compilations", allEntries = true)
    public CompilationDto updateCompilation(Long compilationId, UpdateCompilationRequest compilationDto) {
        Compilation oldCompilation = getCompilationById(compilationId);

        if (compilationDto.pinned() != null) {
            oldCompilation.setPinned(compilationDto.pinned());
        }

        if (compilationDto.title() != null) {
            oldCompilation.setTitle(compilationDto.title());
        }

        final Compilation newCompilation = compilationRepository.save(oldCompilation);

        Set<Event> events = getEvents(compilationDto.eventIds());

        saveCompilationEvents(newCompilation, events);

        return findCompilationById(newCompilation.getId());
    }

    @Override
    @Cacheable(cacheNames = "compilations")
    public List<CompilationDto> findCompilationsByParam(Boolean pinned, Pageable pageable) {

        final List<Compilation> compilationList = compilationRepository.findAll(CompilationRepository.Predicates.buildPredicates(pinned), pageable).toList();

        final Map<Long, Set<Event>> eventsByCompilationId = getEventsByCompilations(compilationList);

        return compilationList.stream()
                .map(e -> compilationMapper.toDto(e, eventsToShortDto(eventsByCompilationId.get(e.getId()))))
                .toList();
    }

    @Override
    public CompilationDto findCompilationById(Long compilationId) {
        final Compilation compilation = getCompilationById(compilationId);

        final Map<Long, Set<Event>> eventsByCompilationId = getEventsByCompilations(List.of(compilation));

        return compilationMapper.toDto(compilation, eventsToShortDto(eventsByCompilationId.get(compilation.getId())));
    }

    private Map<Long, Set<Event>> getEventsByCompilations(List<Compilation> compilations) {
        List<EventCompilationId> eventsList = compilationEventRepository.getEventsByCompilationIds(compilations.stream()
                .map(Compilation::getId)
                .toList());

        return eventsList.stream()
                .collect(Collectors.groupingBy(
                        EventCompilationId::compilationId,
                        Collectors.mapping(EventCompilationId::event, Collectors.toSet())));
    }

    @Transactional
    protected void saveCompilationEvents(final Compilation compilation, Set<Event> events) {

        compilationEventRepository.deleteByCompilationId(compilation.getId());

        if (!events.isEmpty()) {
            List<CompilationEvent> compEvent = events.stream()
                    .map(e -> CompilationEvent.builder()
                            .compilation(compilation)
                            .event(e)
                            .build())
                    .toList();

            compilationEventRepository.saveAll(compEvent);
        }
    }

    private Set<Event> getEvents(Set<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Set.of();
        }

        final Map<Long, Event> eventsById = eventRepository.findAllById(eventIds).stream()
                .collect(Collectors.toMap(Event::getId, Function.identity()));

        eventIds.forEach(eventId -> {
            if (!eventsById.containsKey(eventId)) {
                throw new NotFoundException("Событие id " + eventId + " не найдено");
            }
        });

        return Set.copyOf(eventsById.values());
    }

    private Compilation getCompilationById(Long compilationId) {
        return compilationRepository.findById(compilationId)
                .orElseThrow(() -> new NotFoundException("Compilation " + compilationId + " not found!"));
    }

    private Set<EventShortDto> eventsToShortDto(Set<Event> events) {
        if (events == null) {
            return Set.of();
        }

        Set<Long> userIds = events.stream()
                .map(Event::getInitiatorId)
                .collect(Collectors.toSet());

        Map<Long, UserShortDto> users = userFeignClient.getUsersByIds(userIds).stream()
                .collect(Collectors.toMap(UserDto::id, UserDto::toShortDto));


        return events.stream()
                .map(event -> eventMapper.toShortDto(event, users.get(event.getInitiatorId())))
                .collect(Collectors.toSet());
    }
}
