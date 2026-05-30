package ru.practicum.service.comment;

import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.NewCommentRequest;
import ru.practicum.dto.comment.StateCommentDto;
import ru.practicum.dto.comment.UpdateCommentRequest;
import ru.practicum.model.CommentDateSort;
import ru.practicum.model.CommentState;

import java.util.List;

public interface CommentService {
    CommentDto createComment(Long userId, NewCommentRequest commentDto);

    List<CommentDto> getComments(Long userId);

    CommentDto updateComment(Long userId, UpdateCommentRequest commentDto);

    void deleteComment(Long userId, Long comId);

    List<StateCommentDto> getComments(String text, CommentDateSort sort);

    StateCommentDto reviewComment(Long comId, boolean approved);

    void deleteComment(Long comId);

    List<CommentDto> getCommentsByState(CommentState state, CommentDateSort sort);

    List<CommentDto> getCommentsByEvent(Long eventId, CommentDateSort sort);
}