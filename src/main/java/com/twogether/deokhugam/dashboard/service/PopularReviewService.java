package com.twogether.deokhugam.dashboard.service;

import com.twogether.deokhugam.common.dto.CursorPageResponse;
import com.twogether.deokhugam.dashboard.dto.request.PopularRankingSearchRequest;
import com.twogether.deokhugam.dashboard.dto.response.PopularReviewDto;
import org.springframework.stereotype.Service;

public interface PopularReviewService {

    CursorPageResponse<PopularReviewDto> getPopularReviews(PopularRankingSearchRequest request);
}
