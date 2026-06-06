package ru.practicum.dal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.dal.model.RecommendedEventProjection;
import ru.practicum.dal.model.Similarity;

import java.util.List;
import java.util.Optional;

@Repository
public interface SimilarityRepository extends JpaRepository<Similarity, Long> {
    Optional<Similarity> getSimilarityByEvent1AndEvent2(long event1, long event2);

    @Query("""
                SELECT
                    candidates.eventId as eventId,
                    MAX(candidates.score) as score
                FROM (
                    SELECT
                        CASE
                            WHEN s.event1 IN (:recentEvents) THEN s.event2
                            ELSE s.event1
                        END as eventId,
                        s.similarity as score
                    FROM Similarity s
                    WHERE (s.event1 IN (:recentEvents) AND s.event2 NOT IN (:recentEvents))
                       OR (s.event2 IN (:recentEvents) AND s.event1 NOT IN (:recentEvents))
                ) as candidates
                GROUP BY candidates.eventId
                ORDER BY score DESC
                LIMIT :limit
            """)
    List<RecommendedEventProjection> findRecommendedEvents(
            @Param("recentEvents") List<Long> recentEvents,
            @Param("limit") int limit);

    @Query("""
                SELECT
                        CASE
                            WHEN s.event1 = :candidateId THEN s.event2
                            ELSE s.event1
                        END as eventId,
                        s.similarity as score
                FROM Similarity s
                WHERE (s.event1 = :candidateId AND s.event2 IN (:userEvents))
                   OR (s.event2 = :candidateId AND s.event1 IN (:userEvents))
                ORDER BY s.similarity DESC
                LIMIT :limit
            """)
    List<RecommendedEventProjection> findNearbyEvents(
            @Param("candidateId") long candidateId,
            @Param("userEvents") List<Long> userEvents,
            @Param("limit") int limit);

    @Query("""
            SELECT
                CASE
                    WHEN s.event1 = :eventId THEN s.event2
                    ELSE s.event1
                END as eventId,
                s.similarity as score
            FROM Similarity s
            WHERE (s.event1 IN (:eventId) AND s.event2 NOT IN (:excludedEvents))
               OR (s.event2 IN (:eventId) AND s.event1 NOT IN (:excludedEvents))
            ORDER BY s.similarity DESC
            LIMIT :limit
            """)
    List<RecommendedEventProjection> findSimilarEvents(
            @Param("eventId") long eventId,
            @Param("excludedEvents") List<Long> excludedEvents,
            @Param("limit") int limit);
}
