package ru.practicum.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.annotation.LogAllMethodsAspect;

/**
 * Конфигурация для аспектно-ориентированного логирования.
 * <p>
 * Настраивает и регистрирует аспекты логирования в контексте Spring.
 */
@Configuration
public class LoggingConfig {

    /**
     * Создает и регистрирует аспект для логирования методов.
     * <p>
     * Аспект будет перехватывать выполнение методов в классах,
     * помеченных аннотацией {@link ru.practicum.annotation.LogAllMethods}.
     *
     * @return аспект логирования
     */
    @Bean
    public LogAllMethodsAspect loggingAspect() {
        return new LogAllMethodsAspect();
    }
}