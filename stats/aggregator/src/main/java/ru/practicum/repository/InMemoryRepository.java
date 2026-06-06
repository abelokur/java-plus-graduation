package ru.practicum.repository;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


@Component
public class InMemoryRepository implements BaseRepository {
    private final Map<Long, Map<Long, Double>> minWeightsSums = new ConcurrentHashMap<>(); // Map<Event, Map<Event, S_min>> // числитель
    private final Map<Long, Map<Long, Double>> eventUserWeights = new ConcurrentHashMap<>(); // Map<Event, Map<User, Weight>> //для вычисления числителя

    private final Map<Long, Double> eventWeightsSums = new ConcurrentHashMap<>(); // Map<Event, Sum_Weights> // знаменатель
    private final Map<Long, Set<Long>> userEvents = new ConcurrentHashMap<>(); // Map<User, Set<Event>> // вспомогательная таблица, что бы хранить все события с которыми взаимодействовал пользователь


    @Override
    public void putMinWeightSum(long eventA, long eventB, double sum) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        minWeightsSums
                .computeIfAbsent(first, e -> new ConcurrentHashMap<>())
                .put(second, sum);
    }

    @Override
    public double getMinWeightSum(long eventA, long eventB) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        return minWeightsSums
                .computeIfAbsent(first, e -> new ConcurrentHashMap<>())
                .getOrDefault(second, 0.0);
    }

    @Override
    public void putEventUserWeight(long event, long user, double weight) {
        eventUserWeights
                .computeIfAbsent(event, u -> new ConcurrentHashMap<>())
                .put(user, weight);
    }

    @Override
    public double getEventUserWeight(long event, long user) {
        return eventUserWeights
                .computeIfAbsent(event, e -> new ConcurrentHashMap<>())
                .getOrDefault(user, 0.0);
    }

    @Override
    public void putUserEvent(long user, long event) {
        userEvents
                .computeIfAbsent(user, e -> new HashSet<>())
                .add(event);
    }

    @Override
    public Set<Long> getUserEvents(long user) {
        Set<Long> events = userEvents.get(user);
        return events != null ? events : Collections.emptySet();
    }

    @Override
    public void putEventWeightSum(long event, double sum) {
        eventWeightsSums.put(event, sum);
    }

    @Override
    public double getEventWeightSum(long event) {
        return eventWeightsSums.getOrDefault(event, 0.0);
    }
}
