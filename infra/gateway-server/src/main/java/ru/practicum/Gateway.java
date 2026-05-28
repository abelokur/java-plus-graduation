package ru.practicum;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Главный класс API Gateway приложения.
 * Выступает как единая точка входа для всех микросервисов,
 * обеспечивая маршрутизацию, балансировку нагрузки и безопасность.
 *
 * <p>Основные функции:</p>
 * <ul>
 *   <li>Маршрутизация запросов к соответствующим микросервисам</li>
 *   <li>Балансировка нагрузки между инстансами сервисов</li>
 *   <li>Service Discovery через Eureka/Consul</li>
 *   <li>Загрузка конфигурационных свойств</li>
 * </ul>
 */
@SpringBootApplication
@EnableDiscoveryClient
@ConfigurationPropertiesScan
@Slf4j
public class Gateway {
    public static void main(String[] args) {
        log.info("Starting Gateway");
        SpringApplication.run(Gateway.class, args);
        log.info("Gateway started");
    }
}
