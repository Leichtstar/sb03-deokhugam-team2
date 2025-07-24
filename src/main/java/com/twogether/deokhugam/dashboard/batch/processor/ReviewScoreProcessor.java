package com.twogether.deokhugam.dashboard.batch.processor;

import com.twogether.deokhugam.dashboard.batch.model.ReviewScoreDto;
import com.twogether.deokhugam.dashboard.entity.PopularReviewRanking;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;

@RequiredArgsConstructor
public class ReviewScoreProcessor implements ItemProcessor<ReviewScoreDto, PopularReviewRanking> {

    private final Instant executionTime;

    @Override
    public PopularReviewRanking process(ReviewScoreDto dto) {
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
            .score(dto.calculateScore())
            .rank(0)
            .createdAt(executionTime)
            .build();
    }
}