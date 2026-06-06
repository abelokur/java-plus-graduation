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
@ConfigurationProperties(prefix = "analyzer")
@Slf4j
public class AppConfig {
    private UserAction userAction = new UserAction();
    private EventSimilarity eventSimilarity = new EventSimilarity();

    @PostConstruct
    public void logConfig() {
        log.info("Configuration loaded: {}", this);
    }

    @Getter
    @Setter
    @ToString
    public static class UserAction {
        private Weight weight = new Weight();

        @Getter
        @Setter
        @ToString
        public static class Weight {
            private double view = 0.4;
            private double register = 0.8;
            private double like = 1.0;
        }
    }

    @Getter
    @Setter
    @ToString
    public static class EventSimilarity {
        private int maxInteractions = 10;
        private int maxNearby = 3;
    }
}
