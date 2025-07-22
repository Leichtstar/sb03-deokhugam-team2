package com.twogether.deokhugam.dashboard.batch.model;

import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import java.util.UUID;

public record BookScoreDto(
    UUID bookId,
    String title,
    String author,
    String thumbnailUrl,
    long reviewCount,
    double averageRating,
    RankingPeriod period
) {

    /**
     * 파워 유저 점수 계산을 위한 가중치
     * - 리뷰 수: 40%
     * - 평점: 60%
     */

    private static final double REVIEW_COUNT_WEIGHT = 0.4;
    private static final double AVERAGE_RATING_WEIGHT = 0.6;

    public double calculateScore() {
        double normalizedReviewCount = Math.log1p(reviewCount) / 10.0;
        double normalizedRating = averageRating / 5.0;
        return normalizedReviewCount * REVIEW_COUNT_WEIGHT + normalizedRating * AVERAGE_RATING_WEIGHT;
    }
}
