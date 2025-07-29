package com.twogether.deokhugam.dashboard.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.within;

import com.twogether.deokhugam.dashboard.batch.model.PowerUserScoreDto;
import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("PowerUserScoreDto 점수 계산 테스트")
class PowerUserScoreDtoTest {

    @Test
    void calculateScore_returnsZero_whenAllInputsAreZero() {
        PowerUserScoreDto dto = new PowerUserScoreDto(
            UUID.randomUUID(), "유저", 0.0, 0L, 0L, RankingPeriod.DAILY
        );

        assertThat(dto.calculateScore()).isEqualTo(0.0);
    }

    @ParameterizedTest
    @CsvSource({
        "20.0,0,0",
        "20.0,9,9",
        "40.0,99,0",
        "16.0,0,99",
        "0.0,99,99"
    })
    void calculateScore_returnsExpectedValue(double reviewScoreSum, long likeCount, long commentCount) {
        PowerUserScoreDto dto = new PowerUserScoreDto(
            UUID.randomUUID(), "테스트유저", reviewScoreSum, likeCount, commentCount, RankingPeriod.DAILY
        );

        double reviewScoreComponent = reviewScoreSum * 0.5;
        double likeScoreComponent = Math.log1p(likeCount) / 10.0 * 0.2;
        double commentScoreComponent = Math.log1p(commentCount) / 10.0 * 0.3;

        double expected = reviewScoreComponent + likeScoreComponent + commentScoreComponent;

        assertThat(dto.calculateScore()).isEqualTo(expected, within(1e-6));
    }
}