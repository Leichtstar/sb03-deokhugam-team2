package com.twogether.deokhugam.dashboard.review;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.twogether.deokhugam.dashboard.batch.model.ReviewScoreDto;
import com.twogether.deokhugam.dashboard.batch.processor.ReviewScoreProcessor;
import com.twogether.deokhugam.dashboard.entity.PopularReviewRanking;
import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ReviewScoreProcessor 단위 테스트")
class ReviewScoreProcessorTest {

    ReviewScoreProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new ReviewScoreProcessor(RankingPeriod.DAILY);
    }

    @Test
    @DisplayName("좋아요 5, 댓글 15일 때 점수는 (5 * 0.3 + 15 * 0.7) = 12.0")
    void process_shouldCalculateCorrectScore() {
        // given
        ReviewScoreDto input = new ReviewScoreDto(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "테스터",
            "좋은 책이에요",
            4.0,
            UUID.randomUUID(),
            "이펙티브 자바",
            "http://example.com/image.jpg",
            5L,
            15L
        );

        // when
        PopularReviewRanking result = processor.process(input);

        // then
        assertEquals(12.0, result.getScore(), 0.0001); // double 비교는 delta 설정
        assertEquals(RankingPeriod.DAILY, result.getPeriod());
        assertEquals(input.reviewId(), result.getReviewId());
    }

    @Test
    @DisplayName("좋아요, 댓글이 0일 경우 점수는 0.0")
    void process_shouldReturnZeroScore_whenLikeAndCommentAreZero() {
        ReviewScoreDto input = new ReviewScoreDto(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "테스터",
            "내용 없음",
            3.5,
            UUID.randomUUID(),
            "무제",
            "http://image.png",
            0L,
            0L
        );

        PopularReviewRanking result = processor.process(input);

        assertEquals(0.0, result.getScore(), 0.0001);
    }
}