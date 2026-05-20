package ru.practicum.service.user;

import ru.practicum.dto.user.NewUserRequest;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserParam;

import java.util.List;

public interface UserService {
    UserDto save(NewUserRequest user);

    List<UserDto> findAll(UserParam userParam);

    void deleteById(Long userId);
}