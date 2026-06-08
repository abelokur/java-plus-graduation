package ru.practicum.model.event.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventRequest;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.model.EventState;
import ru.practicum.model.category.Category;
import ru.practicum.model.category.mapper.CategoryMapper;
import ru.practicum.model.event.Event;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {CategoryMapper.class, LocationMapper.class})
public interface EventMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "rating", constant = "0.0")
    @Mapping(target = "confirmedRequests", constant = "0L")
    @Mapping(target = "location", source = "dto.location")
    @Mapping(target = "category", source = "category")
    Event toEntity(NewEventRequest dto, Long initiatorId, Category category, EventState state);

    default Event toEntity(NewEventRequest dto, Long initiatorId, Category category) {
        return toEntity(dto, initiatorId, category, EventState.PENDING);
    }

    @Mapping(target = "id", source = "event.id")
    EventFullDto toFullDto(Event event, UserShortDto initiator);

    @Mapping(target = "id", source = "event.id")
    EventShortDto toShortDto(Event event, UserShortDto initiator);
}