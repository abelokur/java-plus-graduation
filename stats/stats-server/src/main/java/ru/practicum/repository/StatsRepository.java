package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.dto.ResponseStatsDto;
import ru.practicum.model.Stat;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<Stat, Long> {
    @Query("select new ru.practicum.dto.ResponseStatsDto(st.app, st.uri, count(st.ip)) " +
           "from Stat st " +
           "where st.timestamp between :start and :end " +
           "and (:uris is null or st.uri in :uris) " +
           "group by st.app, st.uri " +
           "order by count(st.ip) desc")
    List<ResponseStatsDto> findStats(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("uris") List<String> uris);

    @Query("select new ru.practicum.dto.ResponseStatsDto(st.app, st.uri, count(distinct st.ip)) " +
           "from Stat st " +
           "where st.timestamp between :start and :end " +
           "and (:uris is null or st.uri in :uris) " +
           "group by st.app, st.uri " +
           "order by count(distinct st.ip) desc")
    List<ResponseStatsDto> findUniqueStats(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("uris") List<String> uris);
}
