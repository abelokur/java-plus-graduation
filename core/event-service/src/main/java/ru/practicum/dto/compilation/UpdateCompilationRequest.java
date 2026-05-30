package ru.practicum.dto.compilation;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.Set;

public record UpdateCompilationRequest(
        Boolean pinned,
        @Size(min = 1, max = 50, message = "Заголовок должен быть не менее 1 и не более 50 символов")
        String title,
        @JsonProperty("events")
        Set<Long> eventIds
) {
    @Builder
    public UpdateCompilationRequest {
    }
}
