package ru.practicum.model.event.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.model.category.Category;
import ru.practicum.model.event.Event;
import ru.practicum.model.event.State;
import ru.practicum.model.user.User;

@Mapper(componentModel = "spring",
        imports = {ru.practicum.model.category.mapper.CategoryMapper.class,
                ru.practicum.model.user.mapper.UserMapper.class})
public interface EventMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "initiator", source = "initiator")
    @Mapping(target = "category", source = "category")
    @Mapping(target = "state", source = "state")
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "views", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    Event toEntity(NewEventDto dto, User initiator, Category category, State state);

    default Event toEntity(NewEventDto dto, User initiator, Category category) {
        return toEntity(dto, initiator, category, State.PENDING);
    }

    @Mapping(target = "category", expression = "java(CategoryMapper.mapToCategoryDto(event.getCategory()))")
    @Mapping(target = "initiator", expression = "java(UserMapper.mapToUserShortDto(event.getInitiator()))")
    EventFullDto toFullDto(Event event);

    @Mapping(target = "category", expression = "java(CategoryMapper.mapToCategoryDto(event.getCategory()))")
    @Mapping(target = "initiator", expression = "java(UserMapper.mapToUserShortDto(event.getInitiator()))")
    EventShortDto toShortDto(Event event);
}