package ru.practicum.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UpdateCommentDto(
        @NotNull
        @Positive(message = "id комментария должно быть больше 0")
        Long id,
        @NotBlank(message = "Комментарий не может быть пустым")
        @Size(max = 1000, min = 1, message = "Комментарий должна быть не менее 1 и не более 1000 символов")
        String text
) {
}