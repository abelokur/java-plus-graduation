package ru.practicum.model.comment.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.NewCommentRequest;
import ru.practicum.dto.comment.StateCommentDto;
import ru.practicum.model.CommentState;
import ru.practicum.model.comment.Comment;
import ru.practicum.model.event.Event;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CommentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "text", source = "request.text")
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "event", source = "event")
    @Mapping(target = "state", source = "state")
    Comment toEntity(NewCommentRequest request, Long authorId, Event event, CommentState state);

    @Mapping(target = "author", source = "authorName")
    CommentDto toDto(Comment comment, String authorName);

    @Mapping(target = "author", source = "authorName")
    StateCommentDto toAdminDto(Comment comment, String authorName);
}