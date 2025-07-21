package com.twogether.deokhugam.dashboard.service;

import com.twogether.deokhugam.common.dto.CursorPageResponse;
import com.twogether.deokhugam.common.exception.DeokhugamException;
import com.twogether.deokhugam.common.exception.ErrorCode;
import com.twogether.deokhugam.dashboard.dto.request.PopularRankingSearchRequest;
import com.twogether.deokhugam.dashboard.dto.response.PowerUserDto;
import com.twogether.deokhugam.dashboard.repository.PowerUserRankingRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PowerUserServiceImpl implements PowerUserService {

    private final PowerUserRankingRepository powerUserRankingRepository;

    @Override
    public CursorPageResponse<PowerUserDto> getPowerUsers(PopularRankingSearchRequest request) {
        validateRequest(request);
        Pageable pageable = PageRequest.of(0, request.getLimit());

        List<PowerUserDto> content = powerUserRankingRepository
            .findAllByPeriodWithCursor(request, pageable);

        boolean hasNext = content.size() == request.getLimit();
        String nextCursor = null;
        LocalDateTime nextAfter = null;

        if (hasNext) {
            var last = content.get(content.size() - 1);
            nextCursor = String.valueOf(last.rank());
            nextAfter = last.createdAt();
        }

        return new CursorPageResponse<>(content, nextCursor, nextAfter, request.getLimit(), hasNext);
    }

    private void validateRequest(PopularRankingSearchRequest request) {
        if (request.getPeriod() == null) {
            throw new DeokhugamException(ErrorCode.INVALID_RANKING_PERIOD);
        }

        String direction = request.getDirection();
        if (direction == null) {
            throw new DeokhugamException(ErrorCode.INVALID_DIRECTION);
        }

        direction = direction.toUpperCase(Locale.ROOT);
        if (!direction.equals("ASC") && !direction.equals("DESC")) {
            throw new DeokhugamException(ErrorCode.INVALID_DIRECTION);
        }
    }
}