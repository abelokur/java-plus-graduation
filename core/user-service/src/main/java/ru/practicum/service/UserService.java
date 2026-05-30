package ru.practicum.service;


import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.NewUserRequest;
import ru.practicum.dto.UserParam;
import ru.practicum.dto.user.UserDto;

import java.util.List;
import java.util.Set;

public interface UserService {
    UserDto save(NewUserRequest user);

    List<UserDto> findAll(UserParam userParam);

    void deleteById(Long userId);

    UserDto getUserById(@PathVariable Long id);

    Set<UserDto> getUsersByIds(@RequestParam Set<Long> ids);

}