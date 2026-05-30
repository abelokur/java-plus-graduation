package ru.practicum.dto.event;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import ru.practicum.model.request.RequestStatus;

import java.util.Set;

public record EventRequestStatusUpdateRequest(
        @NotNull(message = "Заявки для обновления должны быть указаны")
        Set<Long> requestIds,
        @NotNull(message = "Статус для обновления должен быть указан")
        RequestStatus status
) {
    @AssertTrue(message = "Пользователь может использовать только SEND_TO_REVIEW или CANCEL_REVIEW")
    public boolean isValidRequestStatus() {
        return status == RequestStatus.CONFIRMED ||
               status == RequestStatus.REJECTED;
    }
}
