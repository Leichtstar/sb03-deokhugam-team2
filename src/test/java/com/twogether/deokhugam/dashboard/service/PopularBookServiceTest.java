package com.twogether.deokhugam.dashboard.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.twogether.deokhugam.common.exception.DeokhugamException;
import com.twogether.deokhugam.common.exception.ErrorCode;
import com.twogether.deokhugam.dashboard.dto.request.PopularRankingSearchRequest;
import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import com.twogether.deokhugam.dashboard.repository.PopularBookRankingRepository;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PopularBookServiceTest {

    private DashboardService dashboardService;
    private PopularBookRankingRepository repository;

    @BeforeEach
    void setUp() {
        repository = mock(PopularBookRankingRepository.class);
        dashboardService = new DashboardServiceImpl(repository);
    }

    @Test
    @DisplayName("정상적으로 인기 도서 목록을 조회할 수 있다")
    void getPopularBooks_success() {
        // given
        PopularRankingSearchRequest request = new PopularRankingSearchRequest();
        request.setPeriod(RankingPeriod.DAILY);
        request.setDirection("ASC");
        request.setLimit(10);

        when(repository.findAllByPeriodWithCursor(eq(request), any()))
            .thenReturn(Collections.emptyList());

        // when
        var response = dashboardService.getPopularBooks(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).isEmpty();
        verify(repository, times(1)).findAllByPeriodWithCursor(eq(request), any());
    }

    @Test
    @DisplayName("정렬 방향이 잘못되면 예외를 던진다")
    void getPopularBooks_invalidDirection() {
        // given
        PopularRankingSearchRequest request = new PopularRankingSearchRequest();
        request.setPeriod(RankingPeriod.DAILY);
        request.setDirection("UPWARD");

        // when & then
        assertThatThrownBy(() -> dashboardService.getPopularBooks(request))
            .isInstanceOf(DeokhugamException.class)
            .hasMessageContaining(ErrorCode.INVALID_DIRECTION.getMessage());
    }

    @Test
    @DisplayName("기간이 null이면 예외를 던진다")
    void getPopularBooks_nullPeriod() {
        // given
        PopularRankingSearchRequest request = new PopularRankingSearchRequest();
        request.setPeriod(null);
        request.setDirection("ASC");

        // when & then
        assertThatThrownBy(() -> dashboardService.getPopularBooks(request))
            .isInstanceOf(DeokhugamException.class)
            .hasMessageContaining(ErrorCode.INVALID_RANKING_PERIOD.getMessage());
    }
}