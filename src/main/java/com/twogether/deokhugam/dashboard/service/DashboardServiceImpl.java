package com.twogether.deokhugam.dashboard.service;

import com.twogether.deokhugam.common.dto.CursorPageResponse;
import com.twogether.deokhugam.common.exception.DeokhugamException;
import com.twogether.deokhugam.common.exception.ErrorCode;
import com.twogether.deokhugam.dashboard.dto.request.PopularRankingSearchRequest;
import com.twogether.deokhugam.dashboard.dto.response.PopularBookDto;
import com.twogether.deokhugam.dashboard.repository.PopularBookRankingRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
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

    private void validateRequest(PopularRankingSearchRequest request) {
        // period 필드 null 체크
        if (request.getPeriod() == null) {
            throw new DeokhugamException(ErrorCode.INVALID_RANKING_PERIOD);
        }

        // direction 필드 Enum 체크 (대소문자 구분 없이 ASC, DESC만 허용)
        String direction = request.getDirection().toUpperCase(Locale.ROOT);
        if (!direction.equals("ASC") && !direction.equals("DESC")) {
            throw new DeokhugamException(ErrorCode.INVALID_DIRECTION);
        }
    }
}
