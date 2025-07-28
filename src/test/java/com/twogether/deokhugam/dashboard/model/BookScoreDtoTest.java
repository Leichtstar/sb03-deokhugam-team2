package com.twogether.deokhugam.dashboard.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.within;

import com.twogether.deokhugam.dashboard.batch.model.BookScoreDto;
import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("BookScoreDto 점수 계산 테스트")
class BookScoreDtoTest {

    @Test
    void calculateScore_returnsZero_whenAllInputsAreZero() {
        BookScoreDto dto = new BookScoreDto(
            UUID.randomUUID(), "책", "저자", "url", 0L, 0.0, RankingPeriod.DAILY
        );

        assertThat(dto.calculateScore()).isEqualTo(0.0);
    }

    @ParameterizedTest
    @CsvSource({
        "1, 5.0",
        "10, 4.0",
        "100, 3.5",
        "0, 2.0",
        "3, 0.0"
    })
    void calculateScore_returnsExpectedValue(long reviewCount, double averageRating) {
        BookScoreDto dto = new BookScoreDto(
            UUID.randomUUID(), "책 제목", "저자", "url", reviewCount, averageRating, RankingPeriod.DAILY
        );

        double normalizedReviewCount = Math.log1p(reviewCount) / 10.0;
        double normalizedRating = averageRating / 5.0;
        double expected = (normalizedReviewCount * 0.4) + (normalizedRating * 0.6);

        assertThat(dto.calculateScore()).isEqualTo(expected, within(1e-6));
    }
}