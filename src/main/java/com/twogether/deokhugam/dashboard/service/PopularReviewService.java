package com.twogether.deokhugam.dashboard.service;

import com.twogether.deokhugam.common.dto.CursorPageResponse;
import com.twogether.deokhugam.dashboard.dto.response.PopularReviewDto;
import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import com.twogether.deokhugam.dashboard.repository.PopularReviewRankingRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PopularReviewService {

    private final PopularReviewRankingRepository repository;

    public PopularReviewService(PopularReviewRankingRepository repository) {
        this.repository = repository;
    }

    public CursorPageResponse<PopularReviewDto> getPopularReviews(
        RankingPeriod period,
        String cursor,
        String after,
        int limit
    ) {
        List<PopularReviewDto> content = repository.findByPeriodWithCursor(period, cursor, after, limit);

        boolean hasNext = content.size() == limit;
        String nextCursor = null;
        LocalDateTime nextAfter = null;

        if (hasNext) {
            PopularReviewDto last = content.get(content.size() - 1);
            nextCursor = last.id().toString();
            nextAfter = last.createdAt();
        }

        return new CursorPageResponse<>(
            content,
            nextCursor,
            nextAfter,
            limit,
            hasNext
        );
    }
}