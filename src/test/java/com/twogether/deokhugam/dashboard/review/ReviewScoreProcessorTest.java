package com.twogether.deokhugam.dashboard.review;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.twogether.deokhugam.dashboard.batch.model.ReviewScoreDto;
import com.twogether.deokhugam.dashboard.batch.processor.ReviewScoreProcessor;
import com.twogether.deokhugam.dashboard.entity.PopularReviewRanking;
import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ReviewScoreProcessor 단위 테스트")
class ReviewScoreProcessorTest {

    ReviewScoreProcessor processor;
    EntityManager em;

    @BeforeEach
    void setUp() {
        em = mock(EntityManager.class);
        processor = new ReviewScoreProcessor(
            Instant.parse("2025-07-22T00:00:00Z"),
            new SimpleMeterRegistry(),
            em
        );
    }

    @Test
    @DisplayName("좋아요 5, 댓글 15일 때 최신 댓글이 1시간 이내이면 bonus 0.003이 반영된다")
    void process_shouldApplyBonus_whenRecentCommentExists() {
        // given
        long like = 5L;
        long comment = 15L;
        UUID reviewId = UUID.randomUUID();

        double expectedBase = Math.log1p(like) / 10 * 0.3
            + Math.log1p(comment) / 10 * 0.7;

        Instant recent = Instant.now().minusSeconds(1800); // 30분 전
        mockLatestCommentTime(reviewId, recent);

        ReviewScoreDto input = new ReviewScoreDto(
            reviewId,
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
        assertNotNull(result);
        assertEquals(expectedBase + 0.003, result.getScore(), 0.0001);
    }

    @Test
    @DisplayName("좋아요, 댓글이 0이고 최신 댓글도 없으면 점수는 0.0")
    void process_shouldReturnZero_whenNoInteraction() {
        UUID reviewId = UUID.randomUUID();
        mockLatestCommentTime(reviewId, null); // 댓글 없음

        ReviewScoreDto input = new ReviewScoreDto(
            reviewId,
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

    private void mockLatestCommentTime(UUID reviewId, Instant time) {
        TypedQuery<Instant> query = mock(TypedQuery.class);
        when(em.createQuery(anyString(), eq(Instant.class))).thenReturn(query);
        when(query.setParameter("reviewId", reviewId)).thenReturn(query);
        when(query.getSingleResult()).thenReturn(time);
    }
}