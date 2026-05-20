package ru.practicum.controller.comment;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.comment.StateCommentDto;
import ru.practicum.model.comment.DateSort;
import ru.practicum.service.comment.CommentService;

import java.util.List;

@RestController
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
@Slf4j
@Validated
public class AdminCommentController {
    private final CommentService commentService;

    @GetMapping
    public List<StateCommentDto> getComments(
            @RequestParam(required = false) String text,
            @RequestParam(defaultValue = "ASC") DateSort sort
    ) {
        log.info("Admin: Method launched (getComments(text = {}, sort = {}))", text, sort);
        return commentService.getComments(text, sort);
    }

    @PatchMapping("/{comId}")
    public StateCommentDto reviewComment(
            @PathVariable @Positive long comId,
            @RequestParam boolean approved
    ) {
        log.info("Admin: Method launched (reviewComment(comId = {}, approved = {}))", comId, approved);
        return commentService.reviewComment(comId, approved);
    }

    @DeleteMapping("/{comId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable @Positive long comId) {
        log.info("Admin: Method launched (deleteComment(comId = {}))", comId);
        commentService.deleteComment(comId);
    }
}
