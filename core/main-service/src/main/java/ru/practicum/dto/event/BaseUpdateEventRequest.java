package ru.practicum.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.DefaultValue;
import lombok.*;
import ru.practicum.model.event.Location;
import ru.practicum.model.event.StateAction;

import java.time.LocalDateTime;

import static ru.practicum.util.DateTimeFormat.DATE_TIME_PATTERN;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseUpdateEventRequest {
    @Size(min = 20, max = 2000, message = "Аннотация должна быть не менее 20 и не более 2000 символов")
    private String annotation;

    private Long category;

    @Size(min = 20, max = 7000, message = "Описание должно быть не менее 20 и не более 7000 символов")
    private String description;

    @JsonFormat(pattern = DATE_TIME_PATTERN)
    @Future(message = "Дата события должна быть в будущем")
    private LocalDateTime eventDate;

    private Location location;

    private Boolean paid;

    @PositiveOrZero(message = "Лимит участников должен быть неотрицательным")
    private Integer participantLimit;

    private Boolean requestModeration;

    private StateAction stateAction = StateAction.NO_ACTION;

    @Size(min = 3, max = 120, message = "Название должно быть не менее 3 и не более 200 символов")
    private String title;

    public abstract boolean isValidStateAction();

    public abstract UpdateType getUpdateType();

    public enum UpdateType {
        USER, ADMIN
    }
}
