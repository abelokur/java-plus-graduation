package ru.practicum.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import ru.practicum.model.RequestStatus;

import java.time.LocalDateTime;

import static ru.practicum.util.DateTimeFormat.DATE_TIME_PATTERN;


public record ParticipationRequestDto(
        Long id,
        RequestStatus status,

        @JsonFormat(pattern = DATE_TIME_PATTERN)
        LocalDateTime created,

        @JsonProperty("event")
        Long eventId,

        @JsonProperty("requester")
        Long requesterId
) {
    @Builder(toBuilder = true)
    public ParticipationRequestDto {
    }
}