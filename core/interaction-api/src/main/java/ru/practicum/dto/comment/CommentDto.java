package ru.practicum.dto.comment;

public record CommentDto(
        Long id,
        String author,
        String text
) {
}