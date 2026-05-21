package ru.practicum.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import ru.practicum.model.request.RequestStatus;

import java.time.LocalDateTime;

import static ru.practicum.util.DateTimeFormat.DATE_TIME_PATTERN;

@Data
@Builder
public class ParticipationRequestDto {
    private Long id;
    private RequestStatus status;
    @JsonFormat(pattern = DATE_TIME_PATTERN)
    private LocalDateTime created;
    @JsonProperty("event")
    private Long eventId;
    @JsonProperty("requester")
    private Long requesterId;
}