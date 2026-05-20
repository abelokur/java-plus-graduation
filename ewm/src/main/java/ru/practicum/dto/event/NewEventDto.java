package ru.practicum.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import ru.practicum.model.event.Location;

import java.time.LocalDateTime;

import static ru.practicum.util.DateTimeFormat.DATE_TIME_PATTERN;

public record NewEventDto(
        @NotBlank(message = "Аннотация не может быть пустой")
        @Size(min = 20, max = 2000, message = "Аннотация должна быть не менее 20 и не более 2000 символов")
        String annotation,

        @NotNull(message = "Категория должна быть заполнена")
        Long category,

        @NotBlank(message = "Описание не может быть пустым")
        @Size(min = 20, max = 7000, message = "Описание должно быть не менее 20 и не более 7000 символов")
        String description,

        @NotNull(message = "Дата события должна быть указана")
        @JsonFormat(pattern = DATE_TIME_PATTERN)
        @Future(message = "Дата события должна быть в будущем")
        LocalDateTime eventDate,

        @NotNull(message = "Место проведения события должно быть указано")
        Location location,

        Boolean paid,
        @PositiveOrZero(message = "Лимит участников должен быть неотрицательным")
        Integer participantLimit,
        Boolean requestModeration,

        @NotBlank(message = "Название должно быть указано")
        @Size(min = 3, max = 120, message = "Название должно быть не менее 3 и не более 200 символов")
        String title
) {
    @Builder(toBuilder = true)
    public NewEventDto {
        paid = paid != null ? paid : false;
        participantLimit = participantLimit != null ? participantLimit : 0;
        requestModeration = requestModeration != null ? requestModeration : true;
    }

}
