package com.twogether.deokhugam.dashboard.repository;

import com.twogether.deokhugam.dashboard.dto.request.PopularRankingSearchRequest;
import com.twogether.deokhugam.dashboard.dto.response.PopularBookDto;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface PopularBookRankingCustomRepository {

    List<PopularBookDto> findAllByPeriodWithCursor(PopularRankingSearchRequest request, Pageable pageable);
}
