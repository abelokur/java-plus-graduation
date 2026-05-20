package ru.practicum.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import ru.practicum.model.event.Location;
import ru.practicum.model.event.StateAction;

import java.time.LocalDateTime;

import static ru.practicum.util.DateTimeFormat.DATE_TIME_PATTERN;

public record UpdateEventUserRequest(
        @Size(min = 20, max = 2000, message = "Аннотация должна быть не менее 20 и не более 2000 символов")
        String annotation,

        Long category,

        @Size(min = 20, max = 7000, message = "Описание должно быть не менее 20 и не более 7000 символов")
        String description,

        @JsonFormat(pattern = DATE_TIME_PATTERN)
        @Future(message = "Дата события должна быть в будущем")
        LocalDateTime eventDate,

        Location location,
        Boolean paid,

        @PositiveOrZero(message = "Лимит участников должен быть неотрицательным")
        Integer participantLimit,
        Boolean requestModeration,
        StateAction stateAction,

        @Size(min = 3, max = 120, message = "Название должно быть не менее 3 и не более 200 символов")
        String title
) {
    public UpdateEventUserRequest {
        stateAction = stateAction != null ? stateAction : StateAction.NO_ACTION;
    }

    @AssertTrue(message = "Пользователь может использовать только SEND_TO_REVIEW или CANCEL_REVIEW")
    public boolean isValidStateAction() {
        return stateAction == StateAction.NO_ACTION ||
               stateAction == StateAction.SEND_TO_REVIEW ||
               stateAction == StateAction.CANCEL_REVIEW;
    }
}
