package com.twogether.deokhugam.dashboard.batch.model;

import java.util.UUID;
import lombok.Builder;

@Builder
public record ReviewSummaryDto(
    UUID reviewId,
    UUID userId,
    String userNickname,
    String content,
    double rating,
    UUID bookId,
    String bookTitle,
    String bookThumbnailUrl,
    long likeCount,
    long commentCount
) {}