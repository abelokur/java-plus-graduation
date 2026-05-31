package ru.practicum.client.request;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.client.RequestClient;

@FeignClient(
        name = "request-service",
        path = "/client/requests",
        fallbackFactory = RequestFeignClientFallbackFactory.class
)
public interface RequestFeignClient extends RequestClient {
}
