package ru.practicum;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableDiscoveryClient
@Slf4j
public class AnalyzerApp {
    public static void main(String[] args) {
        log.info("Starting Analyzer Application");
        SpringApplication.run(AnalyzerApp.class, args);
    }
}