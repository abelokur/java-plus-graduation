package ru.practicum.dal.model.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.config.AppConfig;
import ru.practicum.dal.model.Interaction;
import ru.practicum.dal.model.Similarity;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;

@RequiredArgsConstructor
@Component
public class Mapper {
    private final AppConfig appConfig;

    public Interaction toInteractionFromAvro(UserActionAvro userActionAvro) {
        return Interaction.builder()
                .userId(userActionAvro.getUserId())
                .eventId(userActionAvro.getEventId())
                .rating(getWeight(userActionAvro.getActionType()))
                .createdAt(userActionAvro.getTimestamp())
                .build();
    }

    public Similarity toSimilarityFromAvro(EventSimilarityAvro eventSimilarityAvro) {
        return Similarity.builder()
                .event1(eventSimilarityAvro.getEventA())
                .event2(eventSimilarityAvro.getEventB())
                .similarity(eventSimilarityAvro.getScore())
                .createdAt(eventSimilarityAvro.getTimestamp())
                .build();
    }

    public RecommendedEventProto toRecommendedEventProto(Long eventId, Long score) {
        return RecommendedEventProto.newBuilder()
                .setEventId(eventId)
                .setScore(score)
                .build();
    }

    private double getWeight(ActionTypeAvro actionType) {
        return switch (actionType) {
            case VIEW -> appConfig.getUserAction().getWeight().getView();
            case REGISTER -> appConfig.getUserAction().getWeight().getRegister();
            case LIKE -> appConfig.getUserAction().getWeight().getLike();
        };
    }
}
