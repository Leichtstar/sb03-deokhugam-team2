package com.twogether.deokhugam.dashboard.repository;

import com.twogether.deokhugam.dashboard.dto.response.PopularReviewDto;
import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import java.util.List;

public interface PopularReviewRankingCustomRepository {

    List<PopularReviewDto> findByPeriodWithCursor(
        RankingPeriod period,
        String cursor,
        String after,
        int limits
    );
}