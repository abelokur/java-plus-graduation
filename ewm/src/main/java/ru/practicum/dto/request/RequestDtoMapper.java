package ru.practicum.dto.request;

import ru.practicum.model.request.Request;

import java.util.List;

public class RequestDtoMapper {
    public static ParticipationRequestDto mapRequestToDto(Request request) {
        return ParticipationRequestDto.builder()
                .id(request.getId())
                .status(request.getStatus())
                .created(request.getCreatedOn())
                .eventId(request.getEvent().getId())
                .requesterId(request.getRequester().getId())
                .build();
    }

    public static List<ParticipationRequestDto> mapRequestToDto(List<Request> requests) {
        return requests.stream().map(RequestDtoMapper::mapRequestToDto).toList();
    }
}
