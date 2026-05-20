package ru.practicum.dto.event;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record EventPrivateParam(
        @PositiveOrZero(message = "Параметр from должен быть неотрицательным")
        Integer from,
        @Positive(message = "Параметр size должен быть больше нуля")
        Integer size
) {
    public EventPrivateParam {
        from = from != null ? from : 0;
        size = size != null ? size : 10;
    }
}
