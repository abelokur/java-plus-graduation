package ru.practicum.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.model.event.Location;
import ru.practicum.model.event.State;

import java.time.LocalDateTime;

import static ru.practicum.util.DateTimeFormat.DATE_TIME_PATTERN;

public record EventFullDto(
        Long id,

        @NotBlank
        String annotation,

        @NotNull
        CategoryDto category,

        Long confirmedRequests,

        @JsonFormat(pattern = DATE_TIME_PATTERN)
        LocalDateTime createdOn,

        String description,

        @NotNull
        @JsonFormat(pattern = DATE_TIME_PATTERN)
        LocalDateTime eventDate,

        @NotNull
        UserShortDto initiator,

        @NotNull
        Location location,

        @NotNull
        Boolean paid,

        Integer participantLimit,

        @JsonFormat(pattern = DATE_TIME_PATTERN)
        LocalDateTime publishedOn,

        Boolean requestModeration,

        State state,

        @NotBlank
        String title,

        Long views
) {
    @Builder(toBuilder = true)
    public EventFullDto {
    }
}
