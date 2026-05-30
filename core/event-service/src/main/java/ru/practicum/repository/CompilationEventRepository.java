package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.model.compilation.CompilationEvent;
import ru.practicum.model.compilation.EventCompilationId;

import java.util.List;

public interface CompilationEventRepository extends JpaRepository<CompilationEvent, Long> {

    @Transactional
    @Modifying
    @Query("""
            delete
              from CompilationEvent ce
             where ce.id in (select ce2.id
                               from CompilationEvent ce2
                                    join ce2.compilation as c
                              where c.id = :compId)
            """)
    void deleteByCompilationId(@Param("compId") long compilationId);

    @Query("""
            select new ru.practicum.model.compilation.EventCompilationId(c.id, ce.event)
              from CompilationEvent as ce
                   join ce.compilation as c
             where c.id in (:compIds)
            """)
    List<EventCompilationId> getEventsByCompilationIds(@Param("compIds") List<Long> compilationIds);
}
