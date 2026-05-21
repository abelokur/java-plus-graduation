package ru.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Основной класс для запуска сервера обнаружения сервисов.
 * Использует Eureka Server для регистрации и обнаружения микросервисов.
 */
@SpringBootApplication
@EnableEurekaServer
public class DiscoveryServer {
    /**
     * Точка входа в приложение.
     * Запускает Spring Boot приложение сервера обнаружения.
     *
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        SpringApplication.run(DiscoveryServer.class, args);
    }
}
