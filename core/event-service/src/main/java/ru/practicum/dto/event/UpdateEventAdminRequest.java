package ru.practicum.dto.event;

import jakarta.validation.constraints.AssertTrue;
import ru.practicum.model.EventStateAction;

public class UpdateEventAdminRequest extends BaseUpdateEventRequest {
    @Override
    @AssertTrue(message = "Администратор может использовать только PUBLISH_EVENT или REJECT_EVENT")
    public boolean isValidStateAction() {
        EventStateAction stateAction = getStateAction();
        return stateAction == EventStateAction.NO_ACTION ||
               stateAction == EventStateAction.PUBLISH_EVENT ||
               stateAction == EventStateAction.REJECT_EVENT;
    }

    @Override
    public UpdateType getUpdateType() {
        return UpdateType.ADMIN;
    }
}
