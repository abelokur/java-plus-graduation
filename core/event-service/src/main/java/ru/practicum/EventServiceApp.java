package ru.practicum;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableFeignClients
@EnableDiscoveryClient
@EnableAspectJAutoProxy
@Slf4j
public class EventServiceApp {
    public static void main(String[] args) {
        log.info("Starting EventServiceApp");
        SpringApplication.run(EventServiceApp.class, args);
        log.info("EventServiceApp started");
    }
}