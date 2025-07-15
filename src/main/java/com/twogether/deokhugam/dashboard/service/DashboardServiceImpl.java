package com.twogether.deokhugam.dashboard.service;

import com.twogether.deokhugam.common.dto.CursorPageResponse;
import com.twogether.deokhugam.dashboard.dto.request.PopularRankingSearchRequest;
import com.twogether.deokhugam.dashboard.dto.response.PopularBookDto;
import com.twogether.deokhugam.dashboard.repository.PopularBookRankingRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final PopularBookRankingRepository popularBookRankingRepository;

    @Override
    public CursorPageResponse<PopularBookDto> getPopularBooks(PopularRankingSearchRequest request) {
        Pageable pageable = PageRequest.of(0, request.getLimit());

        List<PopularBookDto> content = popularBookRankingRepository
            .findAllByPeriodWithCursor(request, pageable);

        boolean hasNext = content.size() == request.getLimit();
        String nextCursor = null;
        LocalDateTime nextAfter = null;

        if (hasNext) {
            var last = content.get(content.size() - 1);
            nextCursor = String.valueOf(last.getRank());
            nextAfter = last.getCreatedAt();
        }

        return new CursorPageResponse<>(content, nextCursor, nextAfter, request.getLimit(), hasNext);
    }
}
