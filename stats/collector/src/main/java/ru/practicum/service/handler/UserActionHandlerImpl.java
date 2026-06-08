package ru.practicum.service.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.config.TopicType;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.proto.UserActionProto;
import ru.practicum.service.KafkaUserActionProducer;
import ru.practicum.service.mapper.UserActionMapper;


@Slf4j
@RequiredArgsConstructor
@Component
public class UserActionHandlerImpl implements UserActionHandler {

    private final KafkaUserActionProducer producer;
    private static final TopicType TOPIC_TYPE = TopicType.USER_ACTION;


    @Override
    public void handle(UserActionProto action) {
        if (action == null) {
            throw new IllegalArgumentException("UserAction cannot be null");
        }

        UserActionAvro userActionAvro = UserActionMapper.fromProto(action);

        try {
            producer.sendEvent(TOPIC_TYPE, userActionAvro.getEventId(), userActionAvro);
        } catch (Exception e) {
            log.error("Error processing UserAction. UserActionAvro: {}", userActionAvro, e);
            throw new RuntimeException("Failed to process UserAction", e);
        }
    }
}
