package com.twogether.deokhugam.dashboard.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.twogether.deokhugam.dashboard.batch.model.PowerUserScoreDto;
import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PowerUserScoreDtoTest {

    @Test
    @DisplayName("PowerUserScoreDto 점수 계산 테스트")
    void calculateScore_shouldReturnCorrectScore() {
        // given
        UUID userId = UUID.randomUUID();
        String nickname = "테스트유저";
        double reviewScoreSum = 40.0;
        long likeCount = 100;
        long commentCount = 50;
        RankingPeriod period = RankingPeriod.WEEKLY;

        PowerUserScoreDto dto = new PowerUserScoreDto(
            userId, nickname, reviewScoreSum, likeCount, commentCount, period
        );

        // when
        double score = dto.calculateScore();

        // then
        double expectedReviewScore = 40.0 * 0.5;
        double expectedLikeScore = (Math.log1p(100) / 10.0) * 0.2;
        double expectedCommentScore = (Math.log1p(50) / 10.0) * 0.3;
        double expectedTotalScore = expectedReviewScore + expectedLikeScore + expectedCommentScore;

        assertThat(score).isEqualTo(expectedTotalScore);
    }

    @Test
    @DisplayName("PowerUserScoreDto 점수 계산 - 0건일 경우")
    void calculateScore_withZeroCounts_shouldReturnOnlyReviewScore() {
        // given
        UUID userId = UUID.randomUUID();
        String nickname = "리뷰유저";
        double reviewScoreSum = 10.0;
        long likeCount = 0;
        long commentCount = 0;
        RankingPeriod period = RankingPeriod.DAILY;

        PowerUserScoreDto dto = new PowerUserScoreDto(
            userId, nickname, reviewScoreSum, likeCount, commentCount, period
        );

        // when
        double score = dto.calculateScore();

        // then
        double expectedScore = 10.0 * 0.5;
        assertThat(score).isEqualTo(expectedScore);
    }
}