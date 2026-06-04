package ru.practicum.dto.event;

import jakarta.validation.constraints.AssertTrue;
import ru.practicum.model.EventStateAction;

public class UpdateEventUserRequest extends BaseUpdateEventRequest {
    @Override
    @AssertTrue(message = "Пользователь может использовать только SEND_TO_REVIEW или CANCEL_REVIEW")
    public boolean isValidStateAction() {
        EventStateAction stateAction = getStateAction();
        return stateAction == EventStateAction.NO_ACTION ||
               stateAction == EventStateAction.SEND_TO_REVIEW ||
               stateAction == EventStateAction.CANCEL_REVIEW;
    }

    @Override
    public UpdateType getUpdateType() {
        return UpdateType.USER;
    }
}
