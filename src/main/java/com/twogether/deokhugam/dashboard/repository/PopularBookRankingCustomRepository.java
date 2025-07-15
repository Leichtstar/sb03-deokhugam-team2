package com.twogether.deokhugam.dashboard.repository;

import com.twogether.deokhugam.dashboard.dto.request.PopularRankingSearchRequest;
import com.twogether.deokhugam.dashboard.entity.PopularBookRanking;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface PopularBookRankingCustomRepository {

    List<PopularBookRanking> findAllByPeriodWithCursor(PopularRankingSearchRequest request, Pageable pageable);
}
