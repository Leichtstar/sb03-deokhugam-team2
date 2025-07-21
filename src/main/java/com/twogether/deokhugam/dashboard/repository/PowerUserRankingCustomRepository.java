package com.twogether.deokhugam.dashboard.repository;

import com.twogether.deokhugam.dashboard.dto.request.PopularRankingSearchRequest;
import com.twogether.deokhugam.dashboard.dto.response.PowerUserDto;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface PowerUserRankingCustomRepository {

    List<PowerUserDto> findAllByPeriodWithCursor(PopularRankingSearchRequest request, Pageable pageable);
}