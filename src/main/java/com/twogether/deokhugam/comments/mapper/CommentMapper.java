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
    Comment toEntity(CommentCreateRequest dto);

    @Mapping(target = "userNickname", expression = "java(null)") // 추후 User 연동 시 수정
    CommentResponse toResponse(Comment entity);
}