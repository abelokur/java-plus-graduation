package ru.practicum.controller.comment;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.NewCommentDto;
import ru.practicum.dto.comment.UpdateCommentDto;
import ru.practicum.service.comment.CommentService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/comments")
@RequiredArgsConstructor
@Slf4j
@Validated
public class PrivateCommentController {
    private final CommentService commentService;

    @GetMapping
    public List<CommentDto> getComments(@PathVariable @Positive long userId) {
        log.info("Private: Method launched (getComments({}))", userId);
        return commentService.getComments(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createComment(@PathVariable @Positive long userId,
                                    @RequestBody @Valid NewCommentDto commentDto) {
        log.info("Private: Method launched (createComment({}, {}))", userId, commentDto);
        return commentService.createComment(userId, commentDto);
    }

    @PatchMapping
    public CommentDto updateComment(@PathVariable @Positive long userId,
                                    @RequestBody @Valid UpdateCommentDto commentDto) {
        log.info("Private: Method launched (updateComment({}, {}))", userId, commentDto);
        return commentService.updateComment(userId, commentDto);
    }

    @DeleteMapping("/{comId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable @Positive long userId,
                              @PathVariable @Positive long comId) {
        log.info("Private: Method launched (deleteComment({}, {}))", userId, comId);
        commentService.deleteComment(userId, comId);
    }
}