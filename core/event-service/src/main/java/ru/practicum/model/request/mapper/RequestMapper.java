package ru.practicum.model.request.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.model.request.Request;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RequestMapper {

    @Mapping(target = "created", source = "createdOn")
    @Mapping(target = "eventId", source = "request.event.id")
    ParticipationRequestDto toDto(Request request);

    List<ParticipationRequestDto> toDto(List<Request> requests);

}
