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
import com.twogether.deokhugam.dashboard.dto.response.PowerUserDto;
import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import com.twogether.deokhugam.dashboard.repository.PowerUserRankingRepository;
import java.time.Instant;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PowerUserServiceTest {

    private PowerUserService powerUserService;
    private PowerUserRankingRepository repository;

    @BeforeEach
    void setUp() {
        repository = mock(PowerUserRankingRepository.class);
        powerUserService = new PowerUserServiceImpl(repository);
    }

    @Test
    @DisplayName("정상적으로 파워 유저 목록을 조회할 수 있다")
    void getPowerUsers_success() {
        PopularRankingSearchRequest request = new PopularRankingSearchRequest();
        request.setPeriod(RankingPeriod.DAILY);
        request.setDirection("ASC");
        request.setLimit(10);

        when(repository.findAllByPeriodWithCursor(eq(request), any()))
            .thenReturn(Collections.emptyList());

        var response = powerUserService.getPowerUsers(request);

        assertThat(response).isNotNull();
        assertThat(response.getContent()).isEmpty();
        verify(repository, times(1)).findAllByPeriodWithCursor(eq(request), any());
    }

    @Test
    @DisplayName("정렬 방향이 잘못되면 예외를 던진다")
    void getPowerUsers_invalidDirection() {
        PopularRankingSearchRequest request = new PopularRankingSearchRequest();
        request.setPeriod(RankingPeriod.DAILY);
        request.setDirection("WRONG");

        assertThatThrownBy(() -> powerUserService.getPowerUsers(request))
            .isInstanceOf(DeokhugamException.class)
            .hasMessageContaining(ErrorCode.INVALID_DIRECTION.getMessage());
    }

    @Test
    @DisplayName("기간이 null이면 예외를 던진다")
    void getPowerUsers_nullPeriod() {
        PopularRankingSearchRequest request = new PopularRankingSearchRequest();
        request.setPeriod(null);
        request.setDirection("ASC");

        assertThatThrownBy(() -> powerUserService.getPowerUsers(request))
            .isInstanceOf(DeokhugamException.class)
            .hasMessageContaining(ErrorCode.INVALID_RANKING_PERIOD.getMessage());
    }

    @DisplayName("정렬 방향이 null이면 예외를 던진다")
    @Test
    void getPowerUsers_nullDirection() {
        PopularRankingSearchRequest request = new PopularRankingSearchRequest();
        request.setPeriod(RankingPeriod.DAILY);
        request.setDirection(null);

        assertThatThrownBy(() -> powerUserService.getPowerUsers(request))
            .isInstanceOf(DeokhugamException.class)
            .hasMessageContaining(ErrorCode.INVALID_DIRECTION.getMessage());
    }

    @Test
    @DisplayName("결과 수가 limit보다 작으면 hasNext는 false다")
    void getPowerUsers_hasNextFalse() {
        PopularRankingSearchRequest request = new PopularRankingSearchRequest();
        request.setPeriod(RankingPeriod.DAILY);
        request.setDirection("ASC");
        request.setLimit(10);

        var dto = new PowerUserDto(
            java.util.UUID.randomUUID(),
            "홍길동",
            RankingPeriod.DAILY,
            Instant.now(),
            1,
            99.5,
            50.0,
            20,
            10
        );

        when(repository.findAllByPeriodWithCursor(eq(request), any()))
            .thenReturn(java.util.List.of(dto));

        var response = powerUserService.getPowerUsers(request);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.isHasNext()).isFalse();
    }

    @Test
    @DisplayName("정렬 방향이 DESC인 경우도 정상적으로 조회된다")
    void getPowerUsers_descDirection() {
        PopularRankingSearchRequest request = new PopularRankingSearchRequest();
        request.setPeriod(RankingPeriod.DAILY);
        request.setDirection("DESC");
        request.setLimit(10);

        when(repository.findAllByPeriodWithCursor(eq(request), any()))
            .thenReturn(Collections.emptyList());

        var response = powerUserService.getPowerUsers(request);

        assertThat(response).isNotNull();
        assertThat(response.getContent()).isEmpty();
        assertThat(response.isHasNext()).isFalse();
    }
}
