package ru.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * Основной класс приложения Config Server.
 * Используется для запуска Spring Boot приложения,
 * которое предоставляет конфигурации для других сервисов.
 */
@EnableConfigServer
@SpringBootApplication
public class ConfigServer {
    /**
     * Главный метод для запуска Spring Boot приложения.
     * Запускает контекст Spring Application и инициализирует Config Server.
     *
     * @param args аргументы командной строки, передаваемые в приложение
     */
    public static void main(String[] args) {
        SpringApplication.run(ConfigServer.class, args);
    }
}
