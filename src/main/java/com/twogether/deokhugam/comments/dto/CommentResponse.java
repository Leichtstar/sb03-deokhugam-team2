package com.twogether.deokhugam.comments.dto;

import java.time.Instant;
import java.util.UUID;

public record CommentResponse(
    UUID id,
    String content,
    UUID userId,
    String userNickname,
    UUID reviewId,
    Instant createdAt,
    Instant updatedAt,
    Boolean isDeleted
) {}