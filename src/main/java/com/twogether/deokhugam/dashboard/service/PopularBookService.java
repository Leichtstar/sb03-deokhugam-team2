package com.twogether.deokhugam.dashboard.service;

import com.twogether.deokhugam.common.dto.CursorPageResponse;
import com.twogether.deokhugam.dashboard.dto.request.PopularRankingSearchRequest;
import com.twogether.deokhugam.dashboard.dto.response.PopularBookDto;
import org.springframework.stereotype.Service;

@Service
public interface PopularBookService {

    CursorPageResponse<PopularBookDto> getPopularBooks(PopularRankingSearchRequest request);
}
