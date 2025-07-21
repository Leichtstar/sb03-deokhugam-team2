package com.twogether.deokhugam.dashboard.batch.model;

import java.util.UUID;

public record ReviewScoreDto(
    UUID reviewId,
    UUID userId,
    String userNickname,
    String reviewContent,
    double reviewRating,
    UUID bookId,
    String bookTitle,
    String bookThumbnailUrl,
    long likeCount,
    long commentCount
) {

    /**
     * 파워 유저 점수 계산을 위한 가중치
     * - 좋아요 수: 30%
     * - 댓글 수: 70%
     */

    private static final double LIKE_COUNT_WEIGHT = 0.3;
    private static final double COMMENT_COUNT_WEIGHT = 0.7;

    public double calculateScore() {
        return (likeCount * LIKE_COUNT_WEIGHT) + (commentCount * COMMENT_COUNT_WEIGHT);
    }
}