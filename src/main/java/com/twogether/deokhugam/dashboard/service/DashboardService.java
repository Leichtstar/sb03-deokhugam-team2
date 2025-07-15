package com.twogether.deokhugam.dashboard.service;

import com.twogether.deokhugam.common.dto.CursorPageResponse;
import com.twogether.deokhugam.dashboard.dto.request.PopularRankingSearchRequest;
import com.twogether.deokhugam.dashboard.dto.response.PopularBookDto;

public interface DashboardService {

    CursorPageResponse<PopularBookDto> getPopularBooks(PopularRankingSearchRequest request);
}