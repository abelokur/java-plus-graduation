package ru.practicum.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<List<CommentDto>> getComments(@PathVariable @Positive Long userId) {
        List<CommentDto> comments = commentService.getComments(userId);
        return ResponseEntity.ok(comments);
    }

    @PostMapping
    public ResponseEntity<CommentDto> createComment(@PathVariable @Positive Long userId,
                                                    @RequestBody @Valid NewCommentRequest commentDto) {
        CommentDto created = commentService.createComment(userId, commentDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping
    public ResponseEntity<CommentDto> updateComment(@PathVariable @Positive Long userId,
                                                    @RequestBody @Valid UpdateCommentRequest commentDto) {
        CommentDto updated = commentService.updateComment(userId, commentDto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{comId}")
    public ResponseEntity<Void> deleteComment(@PathVariable @Positive Long userId,
                                              @PathVariable @Positive Long comId) {
        commentService.deleteComment(userId, comId);
        return ResponseEntity.noContent().build();
    }
}