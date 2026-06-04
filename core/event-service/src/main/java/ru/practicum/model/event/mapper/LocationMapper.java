package ru.practicum.model.event.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.practicum.dto.event.LocationDto;
import ru.practicum.model.event.Location;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface LocationMapper {
    LocationDto toDto(Location location);
    Location toEntity(LocationDto locationDto);
}
