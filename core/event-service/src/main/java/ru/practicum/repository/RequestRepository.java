package ru.practicum.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.model.RequestStatus;
import ru.practicum.model.request.Request;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<Request, Long> {

    List<Request> findAllByRequesterId(Long requesterId);

    Optional<Request> findAllByRequesterIdAndEventId(Long requesterId, Long eventId);

    Long countByEventIdAndStatus(Long eventId, RequestStatus status);

    @EntityGraph(attributePaths = "event")
    List<Request> findByEventIdAndEventInitiatorId(Long eventId, Long userId);

    @Query("""
            SELECT r.event.id, COUNT(r)
            FROM Request r
            WHERE r.event.id IN :eventIds AND r.status = 'CONFIRMED'
            GROUP BY r.event.id
            """)
    List<Object[]> countConfirmedRequestsByEventIds(@Param("eventIds") List<Long> eventIds);
}
