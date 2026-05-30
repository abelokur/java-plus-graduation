package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.annotation.LogAllMethods;
import ru.practicum.client.UserClient;
import ru.practicum.dto.user.UserDto;
import ru.practicum.service.UserService;

import java.util.Set;

@RestController
@RequestMapping("/client/users")
@RequiredArgsConstructor
@LogAllMethods
public class UserClientController implements UserClient {
    private final UserService userService;

    @Override
    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @Override
    @GetMapping
    public Set<UserDto> getUsersByIds(@RequestParam Set<Long> ids) {
        return userService.getUsersByIds(ids);
    }
}
