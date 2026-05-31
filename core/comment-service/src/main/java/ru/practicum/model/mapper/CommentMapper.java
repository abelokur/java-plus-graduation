package ru.practicum.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.practicum.dto.NewCommentRequest;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.StateCommentDto;
import ru.practicum.model.Comment;
import ru.practicum.model.CommentState;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CommentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "text", source = "request.text")
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "eventId", source = "eventId")
    @Mapping(target = "state", source = "state")
    Comment toEntity(NewCommentRequest request, Long authorId, Long eventId, CommentState state);

    @Mapping(target = "author", source = "authorName")
    CommentDto toDto(Comment comment, String authorName);

    @Mapping(target = "author", source = "authorName")
    StateCommentDto toAdminDto(Comment comment, String authorName);
}