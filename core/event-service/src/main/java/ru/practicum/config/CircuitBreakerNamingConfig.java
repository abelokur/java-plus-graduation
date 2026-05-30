package ru.practicum.config;

import org.springframework.cloud.openfeign.CircuitBreakerNameResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация именования Circuit Breaker для Feign клиентов.
 */
@Configuration
public class CircuitBreakerNamingConfig {

    /**
     * Создает резолвер имен для Circuit Breaker.
     *
     * @return резолвер, возвращающий имя Feign клиента
     */
    @Bean
    public CircuitBreakerNameResolver circuitBreakerNameResolver() {
        return (feignClientName, target, method) ->
                feignClientName;
    }
}