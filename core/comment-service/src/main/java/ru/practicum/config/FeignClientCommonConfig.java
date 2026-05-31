package ru.practicum.config;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.exception.BaseErrorDecoder;

@Configuration
public class FeignClientCommonConfig {
    @Bean
    public ErrorDecoder baseErrorDecoder() {
        return new BaseErrorDecoder();
    }
}
