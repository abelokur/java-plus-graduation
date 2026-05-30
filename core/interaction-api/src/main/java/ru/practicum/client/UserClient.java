package ru.practicum.client;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.user.UserDto;

import java.util.Set;

public interface UserClient {
    @GetMapping("/{id}")
    UserDto getUserById(@PathVariable Long id);

    @GetMapping
    Set<UserDto> getUsersByIds(@RequestParam Set<Long> ids);
}
