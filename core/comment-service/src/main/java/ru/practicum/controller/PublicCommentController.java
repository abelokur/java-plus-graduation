package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.annotation.LogAllMethods;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.model.CommentDateSort;
import ru.practicum.model.CommentState;
import ru.practicum.service.CommentService;

import java.util.List;

@RestController
@RequestMapping("/events")
@Slf4j
@RequiredArgsConstructor
@Validated
@LogAllMethods
public class PublicCommentController {
    private final CommentService commentService;

    @GetMapping("/comments")
    public List<CommentDto> getAllComments(@RequestParam(name = "sort", defaultValue = "ASC") CommentDateSort sort) {
        return commentService.getCommentsByState(CommentState.APPROVED, sort);
    }

    @GetMapping("/{eventId}/comments")
    public List<CommentDto> getEventComments(@PathVariable(name = "eventId") Long eventId,
                                             @RequestParam(name = "sort", defaultValue = "ASC") CommentDateSort sort) {
        return commentService.getCommentsByEvent(eventId, sort);
    }
}
