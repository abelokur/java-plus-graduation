package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final UserClient userClient;
    private final EventClient eventClient;

    private final CommentRepository commentRepository;

    private final CommentMapper commentMapper;

    @Override
    @Cacheable(cacheNames = "comments")
    public List<CommentDto> getComments(Long userId) {
        log.info("Getting comments for user id: {}", userId);

        ResponseEntity<UserDto> userResponse = userClient.getUserById(userId);
        UserDto userDto = userResponse.getBody();

        List<Comment> comments = commentRepository.findAllByAuthorId(userDto.id());
        log.debug("Found {} comments for user id: {}", comments.size(), userId);

        return comments.stream()
                .map(comment -> commentMapper.toDto(comment, userDto.name()))
                .toList();
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "comments", allEntries = true)
    public CommentDto createComment(Long userId, NewCommentRequest request) {
        log.info("Creating new comment for user id: {}, event id: {}", userId, request.event());

        ResponseEntity<UserDto> userResponse = userClient.getUserById(userId);
        UserDto userDto = userResponse.getBody();

        ResponseEntity<EventFullDto> eventResponse = eventClient.getEventById(request.event());
        EventFullDto event = eventResponse.getBody();

        Comment comment = commentRepository.save(commentMapper
                .toEntity(request, userDto.id(), event.id(), CommentState.WAITING));

        log.info("Comment created successfully with id: {} for user id: {}", comment.getId(), userId);

        return commentMapper.toDto(comment, userDto.name());
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "comments", allEntries = true)
    public CommentDto updateComment(Long userId, UpdateCommentRequest commentDto) {
        log.info("Updating comment id: {} for user id: {}", commentDto.id(), userId);

        ResponseEntity<UserDto> userResponse = userClient.getUserById(userId);
        UserDto userDto = userResponse.getBody();

        Comment comment = commentRepository.findById(commentDto.id())
                .orElseThrow(() -> {
                    log.error("Comment with id {} not found", commentDto.id());
                    return new NotFoundException("Комментария с id " + commentDto.id() + " не найдено");
                });

        if (!userDto.id().equals(comment.getAuthorId())) {
            log.warn("User {} tried to update comment {} but is not the author", userId, commentDto.id());
            throw new AccessDeniedException("Редактировать может только автор комментария");
        }

        comment.setText(commentDto.text());
        CommentDto result = commentMapper.toDto(comment, userDto.name());
        log.info("Comment id: {} updated successfully", comment.getId());

        return result;
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "comments", allEntries = true)
    public void deleteComment(Long userId, Long comId) {
        log.info("Deleting comment id: {} by user id: {}", comId, userId);

        Comment comment = commentRepository.findById(comId)
                .orElseThrow(() -> {
                    log.error("Comment with id {} not found", comId);
                    return new NotFoundException("Комментария с id " + comId + " не найдено");
                });

        if (userId.equals(comment.getAuthorId())) {
            commentRepository.delete(comment);
            log.info("Comment id: {} deleted successfully by user id: {}", comId, userId);
        } else {
            log.warn("User {} tried to delete comment {} but is not the author", userId, comId);
            throw new AccessDeniedException("Удалять комментарий может только автор");
        }
    }

    @Override
    @Cacheable(cacheNames = "comments")
    public List<StateCommentDto> getComments(String text, CommentDateSort sort) {
        log.info("Getting comments with text filter: {} and sort: {}", text, sort);

        Iterable<Comment> commentsIterable = commentRepository
                .findAll(CommentRepository.Predicate.textFilter(text), getSortDate(sort));

        List<Comment> comments = StreamSupport.stream(commentsIterable.spliterator(), false)
                .toList();

        if (comments.isEmpty()) {
            log.debug("No comments found for text filter: {}", text);
            return Collections.emptyList();
        }

        Map<Long, String> authors = getAuthorsNames(comments);
        log.debug("Found {} comments for text filter: {}", comments.size(), text);

        return comments.stream()
                .map(comment -> commentMapper.toAdminDto(comment, authors.get(comment.getAuthorId())))
                .toList();
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "comments", allEntries = true)
    public StateCommentDto reviewComment(Long comId, boolean approved) {
        log.info("Reviewing comment id: {}, approved: {}", comId, approved);

        Comment comment = commentRepository.findById(comId)
                .orElseThrow(() -> {
                    log.error("Comment with id {} not found", comId);
                    return new NotFoundException("Комментария с id " + comId + " не найдено");
                });

        if (!comment.getState().equals(CommentState.WAITING)) {
            log.warn("Comment id: {} has invalid state for review: {}", comId, comment.getState());
            throw new CommentStateException("Подтверждение комментария может осуществляться только если статус равен WAITING");
        }

        if (approved) {
            comment.setState(CommentState.APPROVED);
            log.info("Comment id: {} approved", comId);
        } else {
            comment.setState(CommentState.REJECTED);
            log.info("Comment id: {} rejected", comId);
        }

        ResponseEntity<UserDto> userResponse = userClient.getUserById(comment.getAuthorId());
        UserDto userDto = userResponse.getBody();

        return commentMapper.toAdminDto(comment, userDto.name());
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "comments", allEntries = true)
    public void deleteComment(Long comId) {
        log.info("Deleting comment id: {} by admin", comId);

        Comment comment = commentRepository.findById(comId)
                .orElseThrow(() -> {
                    log.error("Comment with id {} not found", comId);
                    return new NotFoundException("Комментария с id " + comId + " не найдено");
                });

        commentRepository.delete(comment);
        log.info("Comment id: {} deleted successfully by admin", comId);
    }

    @Override
    @Cacheable(cacheNames = "comments")
    public List<CommentDto> getCommentsByState(CommentState state, CommentDateSort sort) {
        log.info("Getting comments by state: {} and sort: {}", state, sort);

        Iterable<Comment> commentsIterable = commentRepository.findAll(CommentRepository.Predicate.stateFilter(state), getSortDate(sort));

        List<Comment> comments = StreamSupport.stream(commentsIterable.spliterator(), false)
                .toList();

        if (comments.isEmpty()) {
            log.debug("No comments found for state: {}", state);
            return Collections.emptyList();
        }

        Map<Long, String> authors = getAuthorsNames(comments);
        log.debug("Found {} comments for state: {}", comments.size(), state);

        return comments.stream()
                .map(comment -> commentMapper.toDto(comment, authors.get(comment.getAuthorId())))
                .toList();
    }

    @Override
    @Cacheable(cacheNames = "comments")
    public List<CommentDto> getCommentsByEvent(Long eventId, CommentDateSort sort) {
        log.info("Getting comments for event id: {} with sort: {}", eventId, sort);

        Iterable<Comment> commentsIterable = commentRepository
                .findAll(CommentRepository.Predicate.eventFilter(eventId), getSortDate(sort));

        List<Comment> comments = StreamSupport.stream(commentsIterable.spliterator(), false)
                .toList();

        if (comments.isEmpty()) {
            log.debug("No comments found for event id: {}", eventId);
            return Collections.emptyList();
        }

        Map<Long, String> authors = getAuthorsNames(comments);
        log.debug("Found {} comments for event id: {}", comments.size(), eventId);

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

        ResponseEntity<Set<UserDto>> usersResponse = userClient.getUsersByIds(authorIds);
        Set<UserDto> users = usersResponse.getBody();

        if (users == null) {
            log.warn("No users found for author ids: {}", authorIds);
            return Collections.emptyMap();
        }

        return users.stream()
                .collect(Collectors.toMap(UserDto::id, UserDto::name));
    }
}