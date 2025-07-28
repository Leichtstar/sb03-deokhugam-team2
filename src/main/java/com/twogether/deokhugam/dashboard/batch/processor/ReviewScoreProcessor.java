package com.twogether.deokhugam.dashboard.batch.processor;

import com.twogether.deokhugam.dashboard.batch.model.ReviewScoreDto;
import com.twogether.deokhugam.dashboard.entity.PopularReviewRanking;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;

@RequiredArgsConstructor
public class ReviewScoreProcessor implements ItemProcessor<ReviewScoreDto, PopularReviewRanking> {

    private final Instant executionTime;
    private final MeterRegistry meterRegistry;

    @Override
    public PopularReviewRanking process(ReviewScoreDto dto) {
        // 처리 시간 측정 시작
        Timer.Sample sample = Timer.start(meterRegistry);

        double score = dto.calculateScore();

        // 커스텀 메트릭 - 계산 건수 카운터 증가
        meterRegistry.counter("batch.popular_review.processed.count").increment();

        // 커스텀 메트릭 - 개별 처리 시간 기록
        sample.stop(Timer.builder("batch.popular_review.processed.timer")
            .description("인기 리뷰 점수 계산에 소요된 시간")
            .tag("reviewId", dto.reviewId().toString())
            .register(meterRegistry));

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
            .score(score)
            .rank(0)
            .createdAt(executionTime)
            .build();
    }
}