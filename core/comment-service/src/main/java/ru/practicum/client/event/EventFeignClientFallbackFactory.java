package ru.practicum.client.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.exception.ServiceTemporaryUnavailableException;

import java.util.Map;

import static ru.practicum.util.FallBackUtility.fastFallBack;

@Component
@Slf4j
public class EventFeignClientFallbackFactory implements FallbackFactory<EventFeignClient> {
    @Override
    public EventFeignClient create(Throwable cause) {
        return new EventFeignClient() {

            @Override
            public ResponseEntity<EventFullDto> getEventById(Long id) {
                fastFallBack(cause);
                throw new ServiceTemporaryUnavailableException(cause.getMessage());
            }

            @Override
            public ResponseEntity<EventFullDto> getEventByIdAndInitiatorId(Long eventId, Long initiatorId) {
                fastFallBack(cause);
                throw new ServiceTemporaryUnavailableException(cause.getMessage());
            }

            @Override
            public ResponseEntity<Void> updateEventsConfirmedRequests(Map<Long, Long> confirmedRequests) {
                fastFallBack(cause);
                throw new ServiceTemporaryUnavailableException(cause.getMessage());
            }
        };
    }
}