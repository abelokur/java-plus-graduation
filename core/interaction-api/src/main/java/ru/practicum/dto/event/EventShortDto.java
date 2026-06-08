package ru.practicum.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.user.UserShortDto;

import java.time.LocalDateTime;

import static ru.practicum.util.DateTimeFormat.DATE_TIME_PATTERN;

public record EventShortDto(
        String annotation,
        CategoryDto category,
        Long confirmedRequests,

        @JsonFormat(pattern = DATE_TIME_PATTERN)
        LocalDateTime eventDate,
        Long id,
        UserShortDto initiator,
        Boolean paid,
        String title,
        Double rating
) {
    @Builder(toBuilder = true)
    public EventShortDto {
    }
}
