package ru.practicum.service;

import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.List;

public interface EventSimilarityService {
    List<EventSimilarityAvro> updateEventSimilarity(UserActionAvro userAction);
}
