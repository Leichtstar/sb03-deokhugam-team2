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
import com.twogether.deokhugam.dashboard.dto.response.PopularBookDto;
import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import com.twogether.deokhugam.dashboard.repository.PopularBookRankingRepository;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PopularBookServiceTest {

    private PopularBookService bookService;
    private PopularBookRankingRepository bookRepository;

    @BeforeEach
    void setUp() {
        bookRepository = mock(PopularBookRankingRepository.class);
        bookService = new PopularBookServiceImpl(bookRepository);
    }

    @Test
    @DisplayName("정상적으로 인기 도서 목록을 조회할 수 있다")
    void getPopularBooks_success() {
        PopularRankingSearchRequest request = new PopularRankingSearchRequest();
        request.setPeriod(RankingPeriod.DAILY);
        request.setDirection("ASC");
        request.setLimit(10);

        when(bookRepository.findAllByPeriodWithCursor(eq(request), any()))
            .thenReturn(Collections.emptyList());

        var response = bookService.getPopularBooks(request);

        assertThat(response).isNotNull();
        assertThat(response.getContent()).isEmpty();
        verify(bookRepository, times(1)).findAllByPeriodWithCursor(eq(request), any());
    }

    @Test
    @DisplayName("정렬 방향이 잘못되면 예외를 던진다")
    void getPopularBooks_invalidDirection() {
        PopularRankingSearchRequest request = new PopularRankingSearchRequest();
        request.setPeriod(RankingPeriod.DAILY);
        request.setDirection("UPWARD");

        assertThatThrownBy(() -> bookService.getPopularBooks(request))
            .isInstanceOf(DeokhugamException.class)
            .hasMessageContaining(ErrorCode.INVALID_DIRECTION.getMessage());
    }

    @Test
    @DisplayName("정렬 방향이 null이면 예외를 던진다")
    void getPopularBooks_nullDirection() {
        PopularRankingSearchRequest request = new PopularRankingSearchRequest();
        request.setPeriod(RankingPeriod.DAILY);
        request.setDirection(null);

        assertThatThrownBy(() -> bookService.getPopularBooks(request))
            .isInstanceOf(DeokhugamException.class)
            .hasMessageContaining(ErrorCode.INVALID_DIRECTION.getMessage());
    }

    @Test
    @DisplayName("기간이 null이면 예외를 던진다")
    void getPopularBooks_nullPeriod() {
        PopularRankingSearchRequest request = new PopularRankingSearchRequest();
        request.setPeriod(null);
        request.setDirection("ASC");

        assertThatThrownBy(() -> bookService.getPopularBooks(request))
            .isInstanceOf(DeokhugamException.class)
            .hasMessageContaining(ErrorCode.INVALID_RANKING_PERIOD.getMessage());
    }

    @Test
    @DisplayName("소문자 정렬 방향도 정상 처리된다")
    void getPopularBooks_lowercaseDirection() {
        PopularRankingSearchRequest request = new PopularRankingSearchRequest();
        request.setPeriod(RankingPeriod.DAILY);
        request.setDirection("asc");

        when(bookRepository.findAllByPeriodWithCursor(eq(request), any()))
            .thenReturn(Collections.emptyList());

        var result = bookService.getPopularBooks(request);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("조회 결과가 limit보다 작으면 hasNext가 false가 된다")
    void getPopularBooks_hasNext_false() {
        PopularRankingSearchRequest request = new PopularRankingSearchRequest();
        request.setPeriod(RankingPeriod.DAILY);
        request.setDirection("ASC");
        request.setLimit(10);

        var dto = new PopularBookDto(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "자바의 정석",
            "남궁성",
            "http://thumbnail",
            RankingPeriod.DAILY,
            1,
            99.0,
            12,
            4.5,
            Instant.now()
        );

        when(bookRepository.findAllByPeriodWithCursor(eq(request), any()))
            .thenReturn(List.of(dto));

        var result = bookService.getPopularBooks(request);

        assertThat(result.isHasNext()).isFalse();
        assertThat(result.getNextCursor()).isNull();
        assertThat(result.getNextAfter()).isNull();
    }

    @Test
    @DisplayName("조회 결과가 limit과 같으면 hasNext가 true가 된다")
    void getPopularBooks_hasNext_true() {
        PopularRankingSearchRequest request = new PopularRankingSearchRequest();
        request.setPeriod(RankingPeriod.DAILY);
        request.setDirection("ASC");
        request.setLimit(1);

        var dto = new PopularBookDto(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "Clean Code",
            "로버트 마틴",
            "http://image",
            RankingPeriod.DAILY,
            1,
            87.0,
            22,
            4.8,
            Instant.now()
        );

        when(bookRepository.findAllByPeriodWithCursor(eq(request), any()))
            .thenReturn(List.of(dto));

        var result = bookService.getPopularBooks(request);

        assertThat(result.isHasNext()).isTrue();
        assertThat(result.getNextCursor()).isEqualTo("1");
        assertThat(result.getNextAfter()).isNotNull();
    }
}