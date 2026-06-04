package ru.practicum.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryDto(
        Long id,

        @NotBlank(message = "Название категории не может быть пустым")
        @Size(min = 1, max = 50, message = "Название категории не может быть больше 50 символов")
        String name
) {
}
