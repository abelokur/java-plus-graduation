package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.NewUserRequest;
import ru.practicum.dto.UserParam;
import ru.practicum.dto.user.UserDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.model.User;
import ru.practicum.model.mapper.UserMapper;
import ru.practicum.repository.UserRepository;
import ru.practicum.util.OffsetBasedPageable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserDto save(NewUserRequest user) {
        User savedUser = userRepository.save(userMapper.toEntity(user));
        return userMapper.toDto(savedUser);
    }

    @Override
    public List<UserDto> findAll(UserParam userParam) {
        Pageable pageable = new OffsetBasedPageable(
                userParam.from(),
                userParam.size(),
                Sort.by("id"));
        List<User> users;

        if (userParam.ids() == null || userParam.ids().isEmpty()) {
            users = userRepository.findAll(pageable).getContent();
        } else {
            users = userRepository.findAllByIdIn(new HashSet<>(userParam.ids()), pageable);
        }
        return users.stream()
                .map(userMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void deleteById(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Нет такого пользователя id = " + userId);
        }
        userRepository.deleteById(userId);
    }

    @Override
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Нет такого пользователя id = " + id));
        return userMapper.toDto(user);
    }

    @Override
    public Set<UserDto> getUsersByIds(Set<Long> ids) {
        Set<User> users = userRepository.findByIdIn(ids);
        if (users.isEmpty()) {
            return Set.of();
        }
        return users.stream()
                .map(userMapper::toDto)
                .collect(Collectors.toSet());
    }
}