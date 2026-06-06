package ru.practicum.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ToString
@ConfigurationProperties("analyzer.user-action.weight")
@Slf4j
public class WeightConfig {
    private double view = 0.4;          // значение по умолчанию
    private double register = 0.8;      // значение по умолчанию
    private double like = 1.0;          // значение по умолчанию

    @PostConstruct
    public void logConfig() {
        log.debug("Configuration loaded with: view={}, register={}, like={}",
                view, register, like);
    }
}
