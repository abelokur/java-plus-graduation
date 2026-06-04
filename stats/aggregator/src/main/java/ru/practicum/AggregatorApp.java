package ru.practicum;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
@ConfigurationPropertiesScan
@Slf4j
public class AggregatorApp {
    public static void main(String[] args) {
        log.info("Starting Aggregator Application");
        SpringApplication.run(AggregatorApp.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("═══════════════════════════════════════════════════");
        log.info("   Aggregator Application STARTED SUCCESSFULLY    ");
        log.info("═══════════════════════════════════════════════════");
    }

    @EventListener(ContextClosedEvent.class)
    public void onApplicationShutdown() {
        log.info("Aggregator Application is shutting down");
    }
}
