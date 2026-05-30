package ru.practicum.dto.event;

import jakarta.validation.constraints.AssertTrue;
import ru.practicum.model.event.StateAction;

public class UpdateEventAdminRequest extends BaseUpdateEventRequest {
    @Override
    @AssertTrue(message = "Администратор может использовать только PUBLISH_EVENT или REJECT_EVENT")
    public boolean isValidStateAction() {
        StateAction stateAction = getStateAction();
        return stateAction == StateAction.NO_ACTION ||
               stateAction == StateAction.PUBLISH_EVENT ||
               stateAction == StateAction.REJECT_EVENT;
    }

    @Override
    public UpdateType getUpdateType() {
        return UpdateType.ADMIN;
    }
}
