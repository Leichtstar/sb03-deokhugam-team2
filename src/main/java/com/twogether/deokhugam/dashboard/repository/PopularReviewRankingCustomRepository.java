package com.twogether.deokhugam.dashboard.repository;

import com.twogether.deokhugam.dashboard.dto.request.PopularRankingSearchRequest;
import com.twogether.deokhugam.dashboard.dto.response.PopularReviewDto;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface PopularReviewRankingCustomRepository {

    List<PopularReviewDto> findAllByPeriodWithCursor(PopularRankingSearchRequest request, Pageable pageable);
}