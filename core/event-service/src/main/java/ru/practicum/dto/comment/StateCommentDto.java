package ru.practicum.dto.comment;

import ru.practicum.model.comment.CommentState;

public record StateCommentDto(
        Long id,
        String author,
        String text,
        CommentState state
) {
}
