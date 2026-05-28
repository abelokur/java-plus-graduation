package ru.practicum.dto.event;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.model.event.EventSort;

import java.time.LocalDateTime;
import java.util.Set;

import static ru.practicum.util.DateTimeFormat.DATE_TIME_PATTERN;

public record EventPublicParam(
        String text,
        Set<Long> categories,
        Boolean paid,

        @DateTimeFormat(pattern = DATE_TIME_PATTERN)
        LocalDateTime rangeStart,

        @DateTimeFormat(pattern = DATE_TIME_PATTERN)
        LocalDateTime rangeEnd,

        Boolean onlyAvailable,
        EventSort sort,

        @PositiveOrZero(message = "Параметр from должен быть неотрицательным")
        Integer from,

        @Positive(message = "Параметр size должен быть больше нуля")
        Integer size
) {
    public EventPublicParam {
        from = from != null ? from : 0;
        size = size != null ? size : 10;
        onlyAvailable = onlyAvailable != null ? onlyAvailable : false;
    }

    @AssertTrue(message = "rangeEnd должен быть после rangeStart")
    public boolean isRangeValid() {
        return rangeStart == null || rangeEnd == null || rangeEnd.isAfter(rangeStart);
    }
}
