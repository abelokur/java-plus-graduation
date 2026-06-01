package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        UserDto user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @Override
    @GetMapping
    public ResponseEntity<Set<UserDto>> getUsersByIds(@RequestParam Set<Long> ids) {
        Set<UserDto> users = userService.getUsersByIds(ids);
        return ResponseEntity.ok(users);
    }
}