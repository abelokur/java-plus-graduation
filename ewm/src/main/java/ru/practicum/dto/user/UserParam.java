package ru.practicum.dto.user;

import java.util.List;

public record UserParam(List<Long> ids, Integer from, Integer size) {
}