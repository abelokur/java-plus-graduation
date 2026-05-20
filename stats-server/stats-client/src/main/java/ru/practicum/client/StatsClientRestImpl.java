package ru.practicum.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.dto.HitCreateDto;
import ru.practicum.dto.RequestStatsDto;
import ru.practicum.dto.ResponseStatsDto;

import java.net.URI;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@Component
public class StatsClientRestImpl implements StatsClient {

    private final String baseUrl;
    private final DateTimeFormatter dateTimeFormatter;
    private final RestClient restClient;

    public StatsClientRestImpl(
            @Value("${stats.service.url}") String baseUrl,
            @Value("${stats.date_time.format}") String dateTamePattern
    ) {
        this.baseUrl = baseUrl;
        this.dateTimeFormatter = DateTimeFormatter.ofPattern(dateTamePattern);
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Override
    public void hit(HitCreateDto dto) {
        try {
            restClient.post()
                    .uri("/hit")
                    .contentType(APPLICATION_JSON)
                    .body(dto)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Failed to send hit to stats service", e);
            throw e;
        }
    }

    @Override
    public List<ResponseStatsDto> get(RequestStatsDto requestStatsDto) {
        try {
            URI uri = UriComponentsBuilder.fromUri(URI.create(baseUrl))
                    .path("/stats")
                    .queryParam("start", requestStatsDto.start().format(dateTimeFormatter))
                    .queryParam("end", requestStatsDto.end().format(dateTimeFormatter))
                    .queryParam("unique", requestStatsDto.unique())
                    .queryParamIfPresent("uris", Optional.ofNullable(requestStatsDto.uris()))
                    .build()
                    .toUri();

            return restClient.get()
                    .uri(uri)
                    .accept(APPLICATION_JSON)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });
        } catch (Exception e) {
            log.warn("Failed to get stats from stats service", e);
            return Collections.emptyList();
        }
    }
}
