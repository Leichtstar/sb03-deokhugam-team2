package com.twogether.deokhugam.dashboard.review;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.twogether.deokhugam.dashboard.batch.model.ReviewScoreDto;
import com.twogether.deokhugam.dashboard.batch.processor.ReviewScoreProcessor;
import com.twogether.deokhugam.dashboard.entity.PopularReviewRanking;
import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ReviewScoreProcessor 단위 테스트")
class ReviewScoreProcessorTest {

    ReviewScoreProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new ReviewScoreProcessor(
            Instant.parse("2025-07-22T00:00:00Z"),
            new SimpleMeterRegistry()
        );
    }

    @Test
    @DisplayName("좋아요 5, 댓글 15일 때 정규화 점수를 계산한다")
    void process_shouldCalculateCorrectScore() {
        // given
        long like = 5L;
        long comment = 15L;

        double expectedScore = Math.log1p(like) / 10 * 0.3
            + Math.log1p(comment) / 10 * 0.7;

        ReviewScoreDto input = new ReviewScoreDto(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "테스터",
            "좋은 책이에요",
            4.0,
            UUID.randomUUID(),
            "이펙티브 자바",
            "http://example.com/image.jpg",
            like,
            comment,
            RankingPeriod.DAILY
        );

        // when
        PopularReviewRanking result = processor.process(input);

        // then
        assertEquals(expectedScore, result.getScore(), 0.0001);
        assertEquals(RankingPeriod.DAILY, result.getPeriod());
        assertEquals(input.reviewId(), result.getReviewId());
        assertEquals(input.userId(), result.getUserId());
        assertEquals(input.userNickname(), result.getUserNickname());
        assertEquals(input.reviewContent(), result.getReviewContent());
        assertEquals(input.reviewRating(), result.getReviewRating());
        assertEquals(input.bookId(), result.getBookId());
        assertEquals(input.bookTitle(), result.getBookTitle());
        assertEquals(input.bookThumbnailUrl(), result.getBookThumbnailUrl());
        assertEquals(0, result.getRank());
        assertNotNull(result.getCreatedAt());
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
            0L,
            RankingPeriod.DAILY
        );

        PopularReviewRanking result = processor.process(input);

        assertEquals(0.0, result.getScore(), 0.0001);
    }
}