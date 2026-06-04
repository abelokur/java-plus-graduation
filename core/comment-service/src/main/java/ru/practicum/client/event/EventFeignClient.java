package ru.practicum.client.event;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.client.EventClient;

@FeignClient(
        name = "event-service",
        path = "/client/events",
        fallbackFactory = EventFeignClientFallbackFactory.class
)
public interface EventFeignClient extends EventClient {
}
