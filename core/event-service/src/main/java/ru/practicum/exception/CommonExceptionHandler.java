package ru.practicum.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class CommonExceptionHandler extends BaseExceptionHandler{
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleCommentStateException(final CommentStateException e) {
        log.warn("400 {}", e.getMessage(), e);
        return new ApiError("BAD_REQUEST", "Несоответствие статуса комментария", e.getMessage());
    }
}
