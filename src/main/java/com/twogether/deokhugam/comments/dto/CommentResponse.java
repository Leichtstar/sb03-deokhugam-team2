package com.twogether.deokhugam.comments.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommentResponse(
    UUID id,
    String content,
    UUID userId,
    String userNickname,
    UUID reviewId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    Boolean isDeleted
) {}