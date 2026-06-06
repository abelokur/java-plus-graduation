package ru.practicum.dal.service;

import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

public interface SimilarityService {
    void saveOrUpdate(EventSimilarityAvro eventSimilarityAvro);
}
