package ru.practicum.model.compilation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.model.compilation.Compilation;

import java.util.Set;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CompilationMapper {

    @Mapping(target = "eventIds", source = "events")
    CompilationDto toDto(Compilation compilation, Set<EventShortDto> events);

}
