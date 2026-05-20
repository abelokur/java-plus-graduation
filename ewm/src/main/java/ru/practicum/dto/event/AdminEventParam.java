package ru.practicum.dto.event;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.model.event.State;

import java.time.LocalDateTime;
import java.util.Set;

import static ru.practicum.util.DateTimeFormat.DATE_TIME_PATTERN;

public record AdminEventParam(
        Set<Long> users,
        Set<State> states,
        Set<Long> categories,

        @DateTimeFormat(pattern = DATE_TIME_PATTERN)
        LocalDateTime rangeStart,

        @DateTimeFormat(pattern = DATE_TIME_PATTERN)
        LocalDateTime rangeEnd,

        @PositiveOrZero(message = "Параметр from должен быть неотрицательным")
        Integer from,

        @Positive(message = "Параметр size должен быть больше нуля")
        Integer size
) {
    public AdminEventParam {
        from = from != null ? from : 0;
        size = size != null ? size : 10;
    }

    @AssertTrue(message = "rangeEnd должен быть после rangeStart")
    public boolean isRangeValid() {
        return rangeStart == null || rangeEnd == null || rangeEnd.isAfter(rangeStart);
    }
}
