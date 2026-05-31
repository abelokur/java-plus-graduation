package ru.practicum.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.annotation.LogAllMethods;
import ru.practicum.dto.comment.StateCommentDto;
import ru.practicum.model.CommentDateSort;
import ru.practicum.service.CommentService;

import java.util.List;

@RestController
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
@Slf4j
@Validated
@LogAllMethods
public class AdminCommentController {
    private final CommentService commentService;

    @GetMapping
    public List<StateCommentDto> getComments(
            @RequestParam(required = false) String text,
            @RequestParam(defaultValue = "ASC") CommentDateSort sort
    ) {
        return commentService.getComments(text, sort);
    }

    @PatchMapping("/{comId}")
    public StateCommentDto reviewComment(
            @PathVariable @Positive Long comId,
            @RequestParam boolean approved
    ) {
        return commentService.reviewComment(comId, approved);
    }

    @DeleteMapping("/{comId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable @Positive Long comId) {
        commentService.deleteComment(comId);
    }
}
