package ru.practicum.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NewUserRequest(
        @NotBlank(message = "Email не может быть пустым")
        @Email(message = "Email должен соответствовать своему формату")
        @Size(min = 6, max = 254, message = "Email не может быть больше 254 символов")
        String email,
        @NotBlank(message = "Имя не может быть пустым")
        @Size(min = 2, max = 250, message = "Имя не может быть больше 250 символов")
        String name
) {
}
