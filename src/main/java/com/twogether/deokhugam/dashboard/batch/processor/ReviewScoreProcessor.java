package com.twogether.deokhugam.dashboard.batch.processor;

import com.twogether.deokhugam.dashboard.batch.model.ReviewScoreDto;
import com.twogether.deokhugam.dashboard.entity.PopularReviewRanking;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;

@Slf4j
@RequiredArgsConstructor
public class ReviewScoreProcessor implements ItemProcessor<ReviewScoreDto, PopularReviewRanking> {

    private final Instant executionTime;
    private final MeterRegistry meterRegistry;
    private final EntityManager em;

    @Override
    public PopularReviewRanking process(ReviewScoreDto dto) {
        // 처리 시간 측정 시작
        Timer.Sample sample = Timer.start(meterRegistry);

        // 정규화 기반 기본 점수 계산
        double baseScore = dto.calculateScore();

        // freshness bonus 계산 (댓글 기준만 사용)
        double freshnessBonus = calculateFreshnessBonus(dto.reviewId());

        double finalScore = baseScore + freshnessBonus;

        // 메트릭 기록
        meterRegistry.counter("batch.popular_review.processed.count").increment();
        sample.stop(Timer.builder("batch.popular_review.processed.timer")
            .description("인기 리뷰 점수 계산에 소요된 시간")
            .tag("reviewId", dto.reviewId().toString())
            .register(meterRegistry));

        // 로그 출력
        log.info("[ReviewScoreProcessor] 리뷰 '{}' (ID: {}) 점수 계산 완료 - 좋아요: {}, 댓글: {}, 정규화 점수: {}, bonus: {}, 최종 점수: {}",
            dto.reviewContent(), dto.reviewId(), dto.likeCount(), dto.commentCount(), baseScore, freshnessBonus, finalScore
        );

        return PopularReviewRanking.builder()
            .period(dto.period())
            .reviewId(dto.reviewId())
            .userId(dto.userId())
            .userNickname(dto.userNickname())
            .reviewContent(dto.reviewContent())
            .reviewRating(dto.reviewRating())
            .bookId(dto.bookId())
            .bookTitle(dto.bookTitle())
            .bookThumbnailUrl(dto.bookThumbnailUrl())
            .likeCount(dto.likeCount())
            .commentCount(dto.commentCount())
            .score(finalScore)
            .rank(0)
            .createdAt(executionTime)
            .build();
    }

    private double calculateFreshnessBonus(UUID reviewId) {
        // 댓글 기준 최신 활동 시간 조회 (논리 삭제 제외)
        TypedQuery<Instant> query = em.createQuery(
            "SELECT MAX(c.createdAt) FROM Comment c " +
                "WHERE c.review.id = :reviewId AND c.isDeleted = false", Instant.class);
        query.setParameter("reviewId", reviewId);
        Instant latestCommentTime = query.getSingleResult();

        return computeBonus(latestCommentTime);
    }

    private double computeBonus(Instant time) {
        if (time == null) return 0.0;

        long hoursAgo = Duration.between(time, Instant.now()).toHours();
        if (hoursAgo < 1) return 0.003;
        if (hoursAgo < 6) return 0.002;
        if (hoursAgo < 24) return 0.001;
        return 0.0;
    }
}