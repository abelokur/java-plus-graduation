package ru.practicum.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Builder;

import java.time.LocalDateTime;

public record HitCreateDto(
        @NotBlank
        String app,

        @NotBlank
        String uri,

        @NotBlank
        String ip,

        @NotNull
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @PastOrPresent(message = "Дата и время совершения запроса к эндпоинту должна быть не позже текущей даты и времени")
        LocalDateTime timestamp
) {
    @Builder(toBuilder = true)
    public HitCreateDto {
    }
}
