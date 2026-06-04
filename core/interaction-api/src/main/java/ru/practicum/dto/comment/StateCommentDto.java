package ru.practicum.dto.comment;


import ru.practicum.model.CommentState;

public record StateCommentDto(
        Long id,
        String author,
        String text,
        CommentState state
) {
}
