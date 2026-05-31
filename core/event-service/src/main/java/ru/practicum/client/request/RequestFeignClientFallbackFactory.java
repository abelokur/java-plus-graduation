package ru.practicum.client.request;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ru.practicum.util.FallBackUtility.fastFallBack;

@Component
public class RequestFeignClientFallbackFactory implements FallbackFactory<RequestFeignClient> {
    @Override
    public RequestFeignClient create(Throwable cause) {
        return new RequestFeignClient() {
            @Override
            public Long getConfirmedRequests(Long eventId) {
                fastFallBack(cause);
                return 0L;
            }

            @Override
            public Map<Long, Long> getConfirmedRequestsForEvents(List<Long> eventIds) {
                fastFallBack(cause);
                return eventIds.stream().collect(Collectors.toMap(Function.identity(), e -> 0L));
            }
        };
    }
}
