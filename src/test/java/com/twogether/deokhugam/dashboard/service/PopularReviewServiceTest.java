package com.twogether.deokhugam.dashboard.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.twogether.deokhugam.common.dto.CursorPageResponse;
import com.twogether.deokhugam.common.exception.DeokhugamException;
import com.twogether.deokhugam.common.exception.ErrorCode;
import com.twogether.deokhugam.dashboard.dto.request.PopularRankingSearchRequest;
import com.twogether.deokhugam.dashboard.dto.response.PopularReviewDto;
import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import com.twogether.deokhugam.dashboard.repository.PopularReviewRankingRepository;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PopularReviewServiceTest {

    private final PopularReviewRankingRepository repository = mock(PopularReviewRankingRepository.class);
    private final PopularReviewService service = new PopularReviewServiceImpl(repository);

    @Test
    @DisplayName("기간별 인기 리뷰 목록을 반환한다")
    void getPopularReviews_shouldReturnReviewsByPeriod() {
        PopularReviewDto dto = createDummyReviewDto();

        PopularRankingSearchRequest request = new PopularRankingSearchRequest();
        request.setPeriod(RankingPeriod.DAILY);
        request.setDirection("DESC");
        request.setLimit(10);

        when(repository.findAllByPeriodWithCursor(eq(request), any()))
            .thenReturn(List.of(dto));

        CursorPageResponse<PopularReviewDto> result = service.getPopularReviews(request);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).bookTitle()).isEqualTo("이펙티브 자바");
    }

    @Test
    @DisplayName("기간이 null이면 예외를 던진다")
    void getPopularReviews_nullPeriod() {
        PopularRankingSearchRequest request = new PopularRankingSearchRequest();
        request.setPeriod(null);
        request.setDirection("ASC");

        assertThatThrownBy(() -> service.getPopularReviews(request))
            .isInstanceOf(DeokhugamException.class)
            .hasMessageContaining(ErrorCode.INVALID_RANKING_PERIOD.getMessage());
    }

    @Test
    @DisplayName("정렬 방향이 null이면 예외를 던진다")
    void getPopularReviews_nullDirection() {
        PopularRankingSearchRequest request = new PopularRankingSearchRequest();
        request.setPeriod(RankingPeriod.DAILY);
        request.setDirection(null);

        assertThatThrownBy(() -> service.getPopularReviews(request))
            .isInstanceOf(DeokhugamException.class)
            .hasMessageContaining(ErrorCode.INVALID_DIRECTION.getMessage());
    }

    @Test
    @DisplayName("소문자 정렬 방향도 정상 처리된다")
    void getPopularReviews_lowercaseDirection() {
        PopularRankingSearchRequest request = new PopularRankingSearchRequest();
        request.setPeriod(RankingPeriod.DAILY);
        request.setDirection("asc");

        when(repository.findAllByPeriodWithCursor(eq(request), any()))
            .thenReturn(Collections.emptyList());

        var result = service.getPopularReviews(request);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("대소문자 혼합 정렬 방향도 정상 처리된다")
    void getPopularReviews_mixedCaseDirection() {
        PopularRankingSearchRequest request = new PopularRankingSearchRequest();
        request.setPeriod(RankingPeriod.DAILY);
        request.setDirection("Asc");

        when(repository.findAllByPeriodWithCursor(eq(request), any()))
            .thenReturn(Collections.emptyList());

        var result = service.getPopularReviews(request);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("조회 결과가 limit보다 작으면 hasNext가 false가 된다")
    void getPopularReviews_hasNext_false() {
        PopularReviewDto dto = createDummyReviewDto();

        PopularRankingSearchRequest request = new PopularRankingSearchRequest();
        request.setPeriod(RankingPeriod.DAILY);
        request.setDirection("DESC");
        request.setLimit(10);

        when(repository.findAllByPeriodWithCursor(eq(request), any()))
            .thenReturn(List.of(dto)); // size = 1 < limit = 10

        var result = service.getPopularReviews(request);

        assertThat(result.isHasNext()).isFalse();
    }

    @Test
    @DisplayName("조회 결과가 limit과 같으면 hasNext가 true가 된다")
    void getPopularReviews_hasNext_true() {
        PopularReviewDto dto = createDummyReviewDto();

        PopularRankingSearchRequest request = new PopularRankingSearchRequest();
        request.setPeriod(RankingPeriod.DAILY);
        request.setDirection("DESC");
        request.setLimit(1);

        when(repository.findAllByPeriodWithCursor(eq(request), any()))
            .thenReturn(List.of(dto)); // size = 1 == limit = 1

        var result = service.getPopularReviews(request);

        assertThat(result.isHasNext()).isTrue();
    }

    private PopularReviewDto createDummyReviewDto() {
        return new PopularReviewDto(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            "이펙티브 자바",
            "https://image.url",
            UUID.randomUUID(),
            "김코딩",
            "이 책은 정말 훌륭해요.",
            5.0,
            RankingPeriod.DAILY,
            LocalDateTime.now(),
            1,
            100.0,
            10,
            3
        );
    }
}