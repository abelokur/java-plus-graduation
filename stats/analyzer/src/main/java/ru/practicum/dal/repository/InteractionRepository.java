package ru.practicum.dal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.dal.model.Interaction;
import ru.practicum.dal.model.RecommendedEventProjection;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface InteractionRepository extends JpaRepository<Interaction, Long> {
    Optional<Interaction> getByUserIdAndEventId(long userId, long eventId);

    @Query("""
            SELECT
                i.eventId as eventId,
                SUM(i.rating) as score
            FROM Interaction i
            WHERE i.eventId IN :eventIds
            GROUP BY i.eventId
            """)
    List<RecommendedEventProjection> getAggregatedRatingByEvent(@Param("eventIds") Set<Long> eventIds);

    @Query("""
            SELECT
                i.eventId as eventId,
                i.rating as score
            FROM Interaction i
            WHERE i.userId = :userId
            ORDER BY i.createdAt DESC
            LIMIT :limit
            """)
    List<RecommendedEventProjection> getRecentUserEvents(
            @Param("userId") long userId,
            @Param("limit") int limit);

    @Query("""
            SELECT i.eventId
            FROM Interaction i
            WHERE i.userId = :userId
            """)
    List<Long> findAllUserEvents(@Param("userId") long userId);
}
