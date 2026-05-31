package ru.practicum.client.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.exception.ServiceTemporaryUnavailableException;

import static ru.practicum.util.FallBackUtility.fastFallBack;

@Component
@Slf4j
public class EventFeignClientFallbackFactory implements FallbackFactory<EventFeignClient> {
    @Override
    public EventFeignClient create(Throwable cause) {
        return new EventFeignClient() {

            @Override
            public EventFullDto getEventById(Long id) {
                fastFallBack(cause);
                throw new ServiceTemporaryUnavailableException(cause.getMessage());
            }

            @Override
            public EventFullDto getEventByIdAndInitiatorId(Long eventId, Long initiatorId) {
                fastFallBack(cause);
                throw new ServiceTemporaryUnavailableException(cause.getMessage());
            }
        };

    }
}
