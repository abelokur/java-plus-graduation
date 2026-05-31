package ru.practicum.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.annotation.LogAllMethods;
import ru.practicum.dto.NewCommentRequest;
import ru.practicum.dto.UpdateCommentRequest;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.service.CommentService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/comments")
@RequiredArgsConstructor
@Slf4j
@Validated
@LogAllMethods
public class PrivateCommentController {
    private final CommentService commentService;

    @GetMapping
    public List<CommentDto> getComments(@PathVariable @Positive Long userId) {
        return commentService.getComments(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createComment(@PathVariable @Positive Long userId,
                                    @RequestBody @Valid NewCommentRequest commentDto) {
        return commentService.createComment(userId, commentDto);
    }

    @PatchMapping
    public CommentDto updateComment(@PathVariable @Positive Long userId,
                                    @RequestBody @Valid UpdateCommentRequest commentDto) {
        return commentService.updateComment(userId, commentDto);
    }

    @DeleteMapping("/{comId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable @Positive Long userId,
                              @PathVariable @Positive Long comId) {
        commentService.deleteComment(userId, comId);
    }
}