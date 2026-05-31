package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.EventClient;
import ru.practicum.client.UserClient;
import ru.practicum.dto.NewCommentRequest;
import ru.practicum.dto.UpdateCommentRequest;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.StateCommentDto;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.exception.AccessDeniedException;
import ru.practicum.exception.CommentStateException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.model.Comment;
import ru.practicum.model.CommentDateSort;
import ru.practicum.model.CommentState;
import ru.practicum.model.mapper.CommentMapper;
import ru.practicum.repository.CommentRepository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final UserClient userClient;
    private final EventClient eventClient;

    private final CommentRepository commentRepository;

    private final CommentMapper commentMapper;

    @Override
    public List<CommentDto> getComments(Long userId) {
        UserDto userDto = userClient.getUserById(userId);

        List<Comment> comments = commentRepository.findAllByAuthorId(userDto.id());

        return comments.stream()
                .map(comment -> commentMapper.toDto(comment, userDto.name()))
                .toList();
    }

    @Override
    @Transactional
    public CommentDto createComment(Long userId, NewCommentRequest request) {
        UserDto userDto = userClient.getUserById(userId);

        EventFullDto event = eventClient.getEventById(request.event());

        Comment comment = commentRepository.save(commentMapper
                .toEntity(request, userDto.id(), event.id(), CommentState.WAITING));

        return commentMapper.toDto(comment, userDto.name());
    }

    @Override
    @Transactional
    public CommentDto updateComment(Long userId, UpdateCommentRequest commentDto) {
        UserDto userDto = userClient.getUserById(userId);

        Comment comment = commentRepository.findById(commentDto.id())
                .orElseThrow(() -> new NotFoundException("Комментария с id " + commentDto.id() + " не найдено"));

        if (!userDto.id().equals(comment.getAuthorId())) {
            throw new AccessDeniedException("Редактировать может только автор комментария");
        }

        comment.setText(commentDto.text());
        return commentMapper.toDto(comment, userDto.name());
    }

    @Override
    @Transactional
    public void deleteComment(Long userId, Long comId) {
        Comment comment = commentRepository.findById(comId)
                .orElseThrow(() -> new NotFoundException("Комментария с id " + comId + " не найдено"));

        if (userId.equals(comment.getAuthorId())) {
            commentRepository.delete(comment);
        } else {
            throw new AccessDeniedException("Удалять комментарий может только автор");
        }
    }

    @Override
    public List<StateCommentDto> getComments(String text, CommentDateSort sort) {
        Iterable<Comment> commentsIterable = commentRepository
                .findAll(CommentRepository.Predicate.textFilter(text), getSortDate(sort));

        List<Comment> comments = StreamSupport.stream(commentsIterable.spliterator(), false)
                .toList();

        if (comments.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, String> authors = getAuthorsNames(comments);

        return comments.stream()
                .map(comment -> commentMapper.toAdminDto(comment, authors.get(comment.getAuthorId())))
                .toList();
    }

    @Override
    @Transactional
    public StateCommentDto reviewComment(Long comId, boolean approved) {
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

        UserDto userDto = userClient.getUserById(comment.getAuthorId());

        return commentMapper.toAdminDto(comment, userDto.name());
    }

    @Override
    @Transactional
    public void deleteComment(Long comId) {
        Comment comment = commentRepository.findById(comId)
                .orElseThrow(() -> new NotFoundException("Комментария с id " + comId + " не найдено"));
        commentRepository.delete(comment);
    }

    @Override
    public List<CommentDto> getCommentsByState(CommentState state, CommentDateSort sort) {
        Iterable<Comment> commentsIterable = commentRepository.findAll(CommentRepository.Predicate.stateFilter(state), getSortDate(sort));

        List<Comment> comments = StreamSupport.stream(commentsIterable.spliterator(), false)
                .toList();

        if (comments.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, String> authors = getAuthorsNames(comments);

        return comments.stream()
                .map(comment -> commentMapper.toDto(comment, authors.get(comment.getAuthorId())))
                .toList();
    }

    @Override
    public List<CommentDto> getCommentsByEvent(Long eventId, CommentDateSort sort) {
        Iterable<Comment> commentsIterable = commentRepository
                .findAll(CommentRepository.Predicate.eventFilter(eventId), getSortDate(sort));

        List<Comment> comments = StreamSupport.stream(commentsIterable.spliterator(), false)
                .toList();

        if (comments.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, String> authors = getAuthorsNames(comments);

        return comments.stream()
                .map(comment -> commentMapper.toDto(comment, authors.get(comment.getAuthorId())))
                .toList();
    }

    private Sort getSortDate(CommentDateSort sort) {
        return (sort == CommentDateSort.DESC) ?
                Sort.by("created").descending() : Sort.by("created").ascending();
    }

    private Map<Long, String> getAuthorsNames(List<Comment> comments) {
        Set<Long> authorIds = comments.stream()
                .map(Comment::getAuthorId)
                .collect(Collectors.toSet());

        return userClient.getUsersByIds(authorIds).stream()
                .collect(Collectors.toMap(UserDto::id, UserDto::name));
    }

}