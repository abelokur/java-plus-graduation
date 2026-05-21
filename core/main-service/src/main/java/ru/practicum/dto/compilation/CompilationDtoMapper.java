package ru.practicum.dto.compilation;

import ru.practicum.dto.event.EventShortDto;
import ru.practicum.model.compilation.Compilation;

import java.util.Set;

public class CompilationDtoMapper {
    public static CompilationDto mapCompilationToDto(Compilation compilation, Set<EventShortDto> events) {
        return CompilationDto.builder()
                .id(compilation.getId())
                .title(compilation.getTitle())
                .pinned(compilation.getPinned())
                .eventIds(events)
                .build();
    }
}
