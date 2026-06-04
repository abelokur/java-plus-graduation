package ru.practicum.client.user;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.client.UserClient;

@FeignClient(
        name = "user-service",
        path = "/client/users",
        fallbackFactory = UserFeignClientFallbackFactory.class
)
public interface UserFeignClient extends UserClient {
}
