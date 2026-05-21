package ru.practicum.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.MaxAttemptsRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.dto.HitCreateDto;
import ru.practicum.dto.RequestStatsDto;
import ru.practicum.dto.ResponseStatsDto;
import ru.practicum.exception.StatsServerUnavailable;

import java.net.URI;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@Component
public class StatsClientRestImpl implements StatsClient {

    private final String statsServiceId;
    private final DateTimeFormatter dateTimeFormatter;
    private final RestClient restClient;
    private final DiscoveryClient discoveryClient;
    private final RetryTemplate retryTemplate = new RetryTemplate();

    public StatsClientRestImpl(
            @Value("${stats.date_time.format:yyyy-MM-dd HH:mm:ss}") String dateTamePattern,
            @Value("${stats.service.id:stats-server}") String statsServiceId,
            DiscoveryClient discoveryClient
    ) {
        this.dateTimeFormatter = DateTimeFormatter.ofPattern(dateTamePattern);
        this.statsServiceId = statsServiceId;
        this.discoveryClient = discoveryClient;
        this.restClient = RestClient.builder()
                .build();

        FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
        fixedBackOffPolicy.setBackOffPeriod(3000L);
        retryTemplate.setBackOffPolicy(fixedBackOffPolicy);

        MaxAttemptsRetryPolicy retryPolicy = new MaxAttemptsRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        retryTemplate.setRetryPolicy(retryPolicy);
    }

    @Override
    public void hit(HitCreateDto dto) {
        try {
            restClient.post()
                    .uri(makeUri("/hit"))
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
            URI uri = UriComponentsBuilder.fromUri(makeUri("/stats"))
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

    private ServiceInstance getInstance() {
        try {
            return discoveryClient
                    .getInstances(statsServiceId)
                    .getFirst();
        } catch (Exception exception) {
            throw new StatsServerUnavailable(
                    "Ошибка обнаружения адреса сервиса статистики с id: " + statsServiceId,
                    exception
            );
        }
    }

    private URI makeUri(String path) {
        ServiceInstance instance = retryTemplate.execute(cxt -> getInstance());
        return URI.create("http://" + instance.getHost() + ":" + instance.getPort() + path);
    }
}
