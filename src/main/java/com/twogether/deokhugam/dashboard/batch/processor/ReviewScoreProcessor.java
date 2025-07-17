package com.twogether.deokhugam.dashboard.batch.processor;

import com.twogether.deokhugam.dashboard.batch.model.ReviewScoreDto;
import com.twogether.deokhugam.dashboard.entity.PopularReviewRanking;
import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;

@RequiredArgsConstructor
public class ReviewScoreProcessor implements ItemProcessor<ReviewScoreDto, PopularReviewRanking> {

    private final RankingPeriod period;

    @Override
    public PopularReviewRanking process(ReviewScoreDto dto) {
        double score = dto.calculateScore();

        return PopularReviewRanking.builder()
            .id(UUID.randomUUID())
            .period(period)
            .reviewId(dto.reviewId())
            .userId(dto.userId())
            .userNickname(dto.userNickname())
            .content(dto.content())
            .rating(dto.rating())
            .bookId(dto.bookId())
            .bookTitle(dto.bookTitle())
            .bookThumbnailUrl(dto.bookThumbnailUrl())
            .likeCount(dto.likeCount())
            .commentCount(dto.commentCount())
            .score(score)
            .rank(0)
            .createdAt(LocalDateTime.now())
            .build();
    }
}