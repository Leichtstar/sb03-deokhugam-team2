package com.twogether.deokhugam.review.dto;

import java.time.Instant;
import java.util.UUID;

public record ReviewDto(
        UUID id,
        UUID bookId,
        String bookTitle,
        String bookThumbnailUrl,
        UUID userId,
        String userNickname,
        String content,
        double rating,
        long likeCount,
        long commentCount,
        boolean likedByMe,
        Instant createdAt,
        Instant updatedAt
) {}
