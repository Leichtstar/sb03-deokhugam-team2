package com.twogether.deokhugam.dashboard.service;

import com.twogether.deokhugam.common.dto.CursorPageResponse;
import com.twogether.deokhugam.dashboard.dto.response.PopularReviewDto;
import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import com.twogether.deokhugam.dashboard.repository.PopularReviewRankingRepository;
import java.util.List;

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
        // 최소 구현: 테스트 통과를 위해 더미 리스트 반환
        List<PopularReviewDto> content = repository.findByPeriodWithCursor(period, cursor, after, limit);

        return new CursorPageResponse<>(content, null, null, limit, content.size(), false);  // 임시 응답
    }
}