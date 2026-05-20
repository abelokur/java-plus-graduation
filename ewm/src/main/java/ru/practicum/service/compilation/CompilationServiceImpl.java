package ru.practicum.service.compilation;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.CompilationDtoMapper;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.model.compilation.Compilation;
import ru.practicum.model.compilation.CompilationEvent;
import ru.practicum.model.compilation.EventCompilationId;
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

    @Override
    public CompilationDto createCompilation(NewCompilationDto compilationDto) {
        Set<Event> events = getEvents(compilationDto.getEventIds());

        final Compilation compilation = compilationRepository.save(Compilation.builder()
                        .title(compilationDto.getTitle())
                        .pinned(compilationDto.getPinned() != null ? compilationDto.getPinned() : false)
                        .build());

        saveCompilationEvents(compilation, events);

        return findCompilationById(compilation.getId());
    }

    @Override
    public void deleteCompilation(long compilationId) {
        Compilation compilation = getCompilationById(compilationId);

        compilationRepository.delete(compilation);
    }

    @Override
    public CompilationDto updateCompilation(long compilationId, UpdateCompilationRequest compilationDto) {
        Compilation oldCompilation = getCompilationById(compilationId);

        if (compilationDto.getPinned() != null) {
            oldCompilation.setPinned(compilationDto.getPinned());
        }

        if (compilationDto.getTitle() != null) {
            oldCompilation.setTitle(compilationDto.getTitle());
        }

        final Compilation newCompilation = compilationRepository.save(oldCompilation);

        Set<Event> events = getEvents(compilationDto.getEventIds());

        saveCompilationEvents(newCompilation, events);

        return findCompilationById(newCompilation.getId());
    }

    @Override
    public List<CompilationDto> findCompilationsByParam(Boolean pinned, Pageable pageable) {

        final List<Compilation> compilationList = compilationRepository.findAll(CompilationRepository.Predicates.buildPredicates(pinned), pageable).toList();

        final Map<Long, Set<Event>> eventsByCompilationId = getEventsByCompilations(compilationList);

        return compilationList.stream()
                .map(e -> CompilationDtoMapper.mapCompilationToDto(e, eventsToShortDto(eventsByCompilationId.get(e.getId()))))
                .toList();
    }

    @Override
    public CompilationDto findCompilationById(long compilationId) {
        final Compilation compilation = getCompilationById(compilationId);

        final Map<Long, Set<Event>> eventsByCompilationId = getEventsByCompilations(List.of(compilation));

        return CompilationDtoMapper.mapCompilationToDto(compilation, eventsToShortDto(eventsByCompilationId.get(compilation.getId())));
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
    private void saveCompilationEvents(final Compilation compilation, Set<Event> events) {

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

    private Compilation getCompilationById(long compilationId) {
        return compilationRepository.findById(compilationId).orElseThrow(() -> new NotFoundException("Compilation " + compilationId + " not found!"));
    }

    private Set<EventShortDto> eventsToShortDto(Set<Event> events) {
        if (events == null) {
            return Set.of();
        }

        return events.stream()
                .map(eventMapper::toShortDto)
                .collect(Collectors.toSet());
    }
}
