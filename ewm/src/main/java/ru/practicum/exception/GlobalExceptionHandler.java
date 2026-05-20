package ru.practicum.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMissingServletRequestParameterException(final MissingServletRequestParameterException e) {
        log.warn("400 {}", e.getMessage(), e);
        return new ApiError("BAD_REQUEST", "Ожидался обязательный параметр", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMethodArgumentTypeMismatchException(final MethodArgumentTypeMismatchException e) {
        log.warn("400 {}", e.getMessage(), e);
        return new ApiError("BAD_REQUEST", "Несоответствие типов параметров", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMethodArgumentNotValidException(final MethodArgumentNotValidException e) {
        log.warn("400 {}", e.getMessage(), e);
        List<String> errors = e.getBindingResult().getFieldErrors().stream()
                .map(error -> String.format("%s: %s", error.getField(), error.getDefaultMessage()))
                .toList();
        return new ApiError("BAD_REQUEST", "Переданные в метод контроллера данные, не проходят " +
                "проверку на валидацию", e.getMessage(), errors);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleCommentStateException(final CommentStateException e) {
        log.warn("400 {}", e.getMessage(), e);
        return new ApiError("BAD_REQUEST", "Несоответствие статуса комментария", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(final NotFoundException e) {
        log.warn("404 {}", e.getMessage(), e);
        return new ApiError("NOT_FOUND", "Обращение к несуществующему ресурсу", e.getMessage());
    }

    @ExceptionHandler({AlreadyExistsException.class, DataIntegrityViolationException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleIntegrityException(final Exception e) {
        log.warn("409 {}", e.getMessage(), e);
        return new ApiError("CONFLICT", "Нарушение ограничения уникальности", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConditionsNotMetException(final ConditionsNotMetException e) {
        log.warn("409 {}", e.getMessage(), e);
        return new ApiError("CONFLICT", "Нарушение ограничений", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationException(final ValidationException e) {
        log.warn("400 {}", e.getMessage(), e);

        return new ApiError("BAD_REQUEST", "Переданные в метод контроллера данные, не проходят " +
                "проверку на валидацию", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleIllegalArgumentException(final IllegalArgumentException e) {
        log.warn("400 {}", e.getMessage(), e);

        return new ApiError("BAD_REQUEST", "Передан неправильный аргумент", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError handleIAccessDeniedException(final AccessDeniedException e) {
        log.warn("403 {}", e.getMessage(), e);

        return new ApiError("FORBIDDEN", "Доступ к этой операции запрещён", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleException(final Exception e) {
        log.warn("500 {}", e.getMessage(), e);
        return new ApiError("INTERNAL_SERVER_ERROR", "На сервере произошла внутренняя ошибка",
                e.getMessage());
    }
}