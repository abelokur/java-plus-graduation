package ru.practicum.client.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.practicum.dto.user.UserDto;
import ru.practicum.exception.ServiceTemporaryUnavailableException;

import java.util.Set;

import static ru.practicum.util.FallBackUtility.fastFallBack;

@Component
@Slf4j
public class UserFeignClientFallbackFactory implements FallbackFactory<UserFeignClient> {
    @Override
    public UserFeignClient create(Throwable cause) {
        return new UserFeignClient() {
            @Override
            public ResponseEntity<UserDto> getUserById(Long id) {
                fastFallBack(cause);
                throw new ServiceTemporaryUnavailableException(cause.getMessage());
            }

            @Override
            public ResponseEntity<Set<UserDto>> getUsersByIds(Set<Long> ids) {
                fastFallBack(cause);
                throw new ServiceTemporaryUnavailableException(cause.getMessage());
            }
        };
    }
}