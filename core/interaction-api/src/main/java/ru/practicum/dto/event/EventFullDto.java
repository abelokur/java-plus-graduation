package ru.practicum.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.model.EventState;

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
        LocationDto location,

        @NotNull
        Boolean paid,

        Integer participantLimit,

        @JsonFormat(pattern = DATE_TIME_PATTERN)
        LocalDateTime publishedOn,

        Boolean requestModeration,

        EventState state,

        @NotBlank
        String title,

        Long views
) {
    @Builder(toBuilder = true)
    public EventFullDto {
    }

    public EventShortDto toShortDto() {
        return EventShortDto.builder()
                .id(this.id())
                .annotation(this.annotation())
                .category(this.category())
                .confirmedRequests(this.confirmedRequests())
                .eventDate(this.eventDate())
                .initiator(this.initiator())
                .paid(this.paid())
                .title(this.title())
                .views(this.views())
                .build();
    }
}
