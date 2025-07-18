package com.twogether.deokhugam.dashboard.service;

import com.twogether.deokhugam.common.dto.CursorPageResponse;
import com.twogether.deokhugam.dashboard.dto.request.PopularRankingSearchRequest;
import com.twogether.deokhugam.dashboard.dto.response.PowerUserDto;

public interface PowerUserService {

    CursorPageResponse<PowerUserDto> getPowerUsers(PopularRankingSearchRequest request);
}