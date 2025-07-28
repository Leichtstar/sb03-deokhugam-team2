package com.twogether.deokhugam.dashboard.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.within;

import com.twogether.deokhugam.dashboard.batch.model.ReviewScoreDto;
import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("ReviewScoreDto 점수 계산 테스트")
class ReviewScoreDtoTest {

    @Test
    void calculateScore_returnsZero_whenLikeAndCommentAreZero() {
        ReviewScoreDto dto = new ReviewScoreDto(
            UUID.randomUUID(), UUID.randomUUID(), "user", "리뷰", 4.0,
            UUID.randomUUID(), "책 제목", "url", 0L, 0L, RankingPeriod.DAILY
        );

        assertThat(dto.calculateScore()).isEqualTo(0.0);
    }

    @ParameterizedTest
    @CsvSource({
        "1,1",
        "5,10",
        "9,9",
        "99,99",
        "1000,500"
    })
    void calculateScore_returnsExpectedNormalizedValue(long like, long comment) {
        ReviewScoreDto dto = new ReviewScoreDto(
            UUID.randomUUID(), UUID.randomUUID(), "user", "리뷰", 4.0,
            UUID.randomUUID(), "책 제목", "url", like, comment, RankingPeriod.DAILY
        );

        double expectedLikeScore = Math.log1p(like) / 10.0 * 0.3;
        double expectedCommentScore = Math.log1p(comment) / 10.0 * 0.7;
        double expectedScore = expectedLikeScore + expectedCommentScore;

        assertThat(dto.calculateScore()).isEqualTo(expectedScore, within(1e-6));
    }
}