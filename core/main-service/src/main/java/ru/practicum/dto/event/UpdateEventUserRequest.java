package ru.practicum.dto.event;

import jakarta.validation.constraints.AssertTrue;
import ru.practicum.model.event.StateAction;

public class UpdateEventUserRequest extends BaseUpdateEventRequest {
    @Override
    @AssertTrue(message = "Пользователь может использовать только SEND_TO_REVIEW или CANCEL_REVIEW")
    public boolean isValidStateAction() {
        StateAction stateAction = getStateAction();
        return stateAction == StateAction.NO_ACTION ||
               stateAction == StateAction.SEND_TO_REVIEW ||
               stateAction == StateAction.CANCEL_REVIEW;
    }

    @Override
    public UpdateType getUpdateType() {
        return UpdateType.USER;
    }
}
