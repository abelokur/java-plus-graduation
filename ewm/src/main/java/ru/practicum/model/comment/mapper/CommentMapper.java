package ru.practicum.model.comment.mapper;

import ru.practicum.dto.comment.StateCommentDto;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.NewCommentDto;
import ru.practicum.model.comment.Comment;
import ru.practicum.model.comment.CommentState;
import ru.practicum.model.event.Event;
import ru.practicum.model.user.User;

import java.time.LocalDateTime;

public class CommentMapper {
    public static Comment mapToComment(NewCommentDto commentDto, User author, Event event, CommentState state) {
        return Comment.builder()
                .author(author)
                .event(event)
                .text(commentDto.text())
                .state(state)
                .created(LocalDateTime.now())
                .build();
    }

    public static CommentDto mapToCommentDto(Comment comment) {
        return new CommentDto(comment.getId(), comment.getAuthor().getName(), comment.getText());
    }

    public static StateCommentDto mapToAdminDto(Comment comment) {
        return new StateCommentDto(comment.getId(), comment.getAuthor().getName(), comment.getText(), comment.getState());
    }
}