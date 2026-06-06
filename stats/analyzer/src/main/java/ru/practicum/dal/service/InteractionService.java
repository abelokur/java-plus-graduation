package ru.practicum.dal.service;

import ru.practicum.ewm.stats.avro.UserActionAvro;

public interface InteractionService {
    void saveOrUpdate(UserActionAvro userActionAvro);
}
