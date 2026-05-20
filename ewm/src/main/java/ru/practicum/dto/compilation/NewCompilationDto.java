package ru.practicum.dto.compilation;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class NewCompilationDto {
    private Boolean pinned;
    @Size(min = 1, max = 50, message = "Заголовок должен быть не менее 1 и не более 50 символов")
    @NotBlank(message = "Заголовок не может быть пустым")
    private String title;
    @JsonProperty("events")
    private Set<Long> eventIds;
}
