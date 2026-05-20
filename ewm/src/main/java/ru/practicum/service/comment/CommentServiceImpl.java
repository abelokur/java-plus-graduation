package ru.practicum.service.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.comment.StateCommentDto;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.NewCommentDto;
import ru.practicum.dto.comment.UpdateCommentDto;
import ru.practicum.exception.AccessDeniedException;
import ru.practicum.exception.CommentStateException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.model.comment.Comment;
import ru.practicum.model.comment.DateSort;
import ru.practicum.model.comment.mapper.CommentMapper;
import ru.practicum.model.comment.CommentState;
import ru.practicum.model.event.Event;
import ru.practicum.model.user.User;
import ru.practicum.repository.CommentRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.UserRepository;

import java.util.List;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    public List<CommentDto> getComments(long userId) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        List<Comment> comments = commentRepository.findAllByAuthor(author);

        return comments.stream()
                .map(CommentMapper::mapToCommentDto)
                .toList();
    }

    @Override
    @Transactional
    public CommentDto createComment(long userId, NewCommentDto commentDto) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        Event event = eventRepository.findById(commentDto.event())
                .orElseThrow(() -> new NotFoundException("Событие с id " + commentDto.event() + " не найдено"));

        Comment comment = commentRepository.save(CommentMapper.mapToComment(commentDto, author, event, CommentState.WAITING));
        return CommentMapper.mapToCommentDto(comment);
    }

    @Override
    @Transactional
    public CommentDto updateComment(long userId, UpdateCommentDto commentDto) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        Comment comment = commentRepository.findById(commentDto.id())
                .orElseThrow(() -> new NotFoundException("Комментария с id " + commentDto.id() + " не найдено"));

        if (comment.getAuthor() != author) {
            throw new AccessDeniedException("Редактировать может только автор комментария");
        }
        comment.setText(commentDto.text());
        return CommentMapper.mapToCommentDto(comment);
    }

    @Override
    @Transactional
    public void deleteComment(long userId, long comId) {
        Comment comment = commentRepository.findById(comId)
                .orElseThrow(() -> new NotFoundException("Комментария с id " + comId + " не найдено"));
        if (comment.getAuthor().getId() == userId) {
            commentRepository.delete(comment);
        } else {
            throw new AccessDeniedException("Удалять комментарий может только автор");
        }
    }

    @Override
    public List<StateCommentDto> getComments(String text, DateSort sort) {
        Iterable<Comment> comments = commentRepository.findAll(CommentRepository.Predicate.textFilter(text), getSortDate(sort));
        return StreamSupport.stream(comments.spliterator(), false)
                .map(CommentMapper::mapToAdminDto)
                .toList();
    }

    @Override
    @Transactional
    public StateCommentDto reviewComment(long comId, boolean approved) {
        Comment comment = commentRepository.findById(comId)
                .orElseThrow(() -> new NotFoundException("Комментария с id " + comId + " не найдено"));

        if (!comment.getState().equals(CommentState.WAITING)) {
            throw new CommentStateException("Подтверждение комментария может осуществляться только если статус равен WAITING");
        }

        if (approved) {
            comment.setState(CommentState.APPROVED);
        } else {
            comment.setState(CommentState.REJECTED);
        }

        return CommentMapper.mapToAdminDto(comment);
    }

    @Override
    @Transactional
    public void deleteComment(long comId) {
        Comment comment = commentRepository.findById(comId)
                .orElseThrow(() -> new NotFoundException("Комментария с id " + comId + " не найдено"));
        commentRepository.delete(comment);
    }

    @Override
    public List<CommentDto> getCommentsByState(CommentState state, DateSort sort) {
        Iterable<Comment> comments = commentRepository.findAll(CommentRepository.Predicate.stateFilter(state), getSortDate(sort));
        return StreamSupport.stream(comments.spliterator(), false)
                .map(CommentMapper::mapToCommentDto)
                .toList();
    }

    @Override
    public List<CommentDto> getCommentsByEvent(long eventId, DateSort sort) {
        Iterable<Comment> comments = commentRepository.findAll(CommentRepository.Predicate.eventFilter(eventId), getSortDate(sort));
        return StreamSupport.stream(comments.spliterator(), false)
                .map(CommentMapper::mapToCommentDto)
                .toList();
    }

    private Sort getSortDate(DateSort sort) {
        return (sort == DateSort.DESC) ?
                Sort.by("created").descending() : Sort.by("created").ascending();
    }

}