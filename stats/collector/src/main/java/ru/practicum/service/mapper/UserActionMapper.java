package ru.practicum.service.mapper;


import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.proto.ActionTypeProto;
import ru.practicum.ewm.stats.proto.UserActionProto;

import java.util.Collections;
import java.util.List;

import static ru.practicum.util.Converter.timestampToInstant;


public class UserActionMapper {

    public static UserActionAvro fromProto(UserActionProto action) {
        if (action == null) return null;
        return UserActionAvro.newBuilder()
                .setUserId(action.getUserId())
                .setEventId(action.getEventId())
                .setActionType(mapActionType(action.getActionType()))
                .setTimestamp(timestampToInstant(action.getTimestamp()))
                .build();
    }

    public static List<UserActionAvro> fromProto(List<UserActionProto> actions) {
        if (actions == null || actions.isEmpty()) return Collections.emptyList();
        return actions.stream().map(UserActionMapper::fromProto).toList();
    }

    private static ActionTypeAvro mapActionType(ActionTypeProto actionType) {
        return switch (actionType) {
            case ACTION_VIEW -> ActionTypeAvro.VIEW;
            case ACTION_LIKE -> ActionTypeAvro.LIKE;
            case ACTION_REGISTER -> ActionTypeAvro.REGISTER;
            default -> throw new IllegalArgumentException("Unknown action type: " + actionType);
        };
    }
}
