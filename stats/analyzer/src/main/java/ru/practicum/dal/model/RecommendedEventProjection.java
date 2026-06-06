package ru.practicum.dal.model;

import ru.practicum.ewm.stats.proto.RecommendedEventProto;

public interface RecommendedEventProjection {
    Long getEventId();

    Double getScore();

    default RecommendedEventProto toProto() {
        return RecommendedEventProto.newBuilder()
                .setEventId(getEventId())
                .setScore(getScore())
                .build();
    }
}
