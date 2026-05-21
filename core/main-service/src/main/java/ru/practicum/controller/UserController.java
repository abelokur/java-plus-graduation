package ru.practicum.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.user.NewUserRequest;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserParam;
import ru.practicum.service.user.UserService;

import java.util.List;

@RestController
@RequestMapping("/admin")
@Slf4j
@RequiredArgsConstructor
@Validated
public class UserController {
    private final UserService userService;

    @GetMapping("/users")
    public List<UserDto> findAll(
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size
    ) {
        log.info("Method launched (findAll(List<Long> ids = {}, Integer from = {}, Integer size = {}))", ids, from,
                size);
        UserParam userParam = new UserParam(ids, from, size);
        return userService.findAll(userParam);
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto save(@RequestBody @Valid NewUserRequest user) {
        log.info("Method launched (save(UserCreateDto user = {}))", user);
        return userService.save(user);
    }

    @DeleteMapping("/users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable @Positive Long userId) {
        log.info("Method launched (deleteById(Long userId = {}))", userId);
        userService.deleteById(userId);
    }
}