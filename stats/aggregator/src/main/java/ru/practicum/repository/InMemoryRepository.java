package ru.practicum.repository;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@Component
public class InMemoryRepository implements BaseRepository {
    private final Map<Long, Map<Long, Double>> minWeightsSums = new ConcurrentHashMap<>();
    private final Map<Long, Map<Long, Double>> eventUserWeights = new ConcurrentHashMap<>();

    private final Map<Long, Double> eventWeightsSums = new ConcurrentHashMap<>();
    private final Map<Long, Set<Long>> userEvents = new ConcurrentHashMap<>();


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
    public Optional<Double> getEventUserWeight(long event, long user) {
        return Optional.ofNullable(eventUserWeights.get(event))
                .flatMap(userWeights -> Optional.ofNullable(userWeights.get(user)));
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
