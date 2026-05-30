package ru.practicum.dto.compilation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import ru.practicum.dto.event.EventShortDto;

import java.util.Set;

public record CompilationDto(
        Long id,
        String title,
        boolean pinned,
        @JsonProperty("events")
        Set<EventShortDto> eventIds
) {
    @Builder
    public CompilationDto {
    }
}
