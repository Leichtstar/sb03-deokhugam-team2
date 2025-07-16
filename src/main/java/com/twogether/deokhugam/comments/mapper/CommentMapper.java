package com.twogether.deokhugam.comments.mapper;

import com.twogether.deokhugam.comments.dto.CommentCreateRequest;
import com.twogether.deokhugam.comments.dto.CommentResponse;
import com.twogether.deokhugam.comments.entity.Comment;
import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface CommentMapper {

    @BeanMapping(builder = @Builder(disableBuilder = true))
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "review", ignore = true)
    Comment toEntity(CommentCreateRequest dto);

    @Mapping(target = "userNickname", source = "user.nickname")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "reviewId", source = "review.id")
    CommentResponse toResponse(Comment entity);
}