package com.twogether.deokhugam.dashboard.batch.model;

import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import java.util.UUID;

public record PowerUserScoreDto(
    UUID userId,
    String nickname,
    double reviewScoreSum,
    long likeCount,
    long commentCount,
    RankingPeriod period
) {

    /**
     * 파워 유저 점수 계산을 위한 가중치
     * - 리뷰 점수: 50%
     * - 좋아요 수: 20%
     * - 댓글 수: 30%
     */

    private static final double REVIEW_SCORE_WEIGHT = 0.5;
    private static final double LIKE_COUNT_WEIGHT = 0.2;
    private static final double COMMENT_COUNT_WEIGHT = 0.3;

    public double calculateScore() {
        return (reviewScoreSum * REVIEW_SCORE_WEIGHT)
            + (likeCount * LIKE_COUNT_WEIGHT)
            + (commentCount * COMMENT_COUNT_WEIGHT);
    }
}