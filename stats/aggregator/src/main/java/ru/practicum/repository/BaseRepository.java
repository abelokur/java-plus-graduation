package ru.practicum.repository;

import java.util.Set;

public interface BaseRepository {
    void putMinWeightSum(long eventA, long eventB, double sum);

    double getMinWeightSum(long eventA, long eventB);

    void putEventUserWeight(long event, long user, double weight);

    double getEventUserWeight(long event, long user);

    void putUserEvent(long user, long event);

    Set<Long> getUserEvents(long user);

    void putEventWeightSum(long event, double sum);

    double getEventWeightSum(long event);
}
