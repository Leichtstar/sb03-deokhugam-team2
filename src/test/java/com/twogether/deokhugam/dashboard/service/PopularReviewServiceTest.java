package com.twogether.deokhugam.dashboard.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.twogether.deokhugam.common.dto.CursorPageResponse;
import com.twogether.deokhugam.dashboard.dto.request.PopularRankingSearchRequest;
import com.twogether.deokhugam.dashboard.dto.response.PopularReviewDto;
import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import com.twogether.deokhugam.dashboard.repository.PopularReviewRankingRepository;
import java.time.LocalDateTime;
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
        // given
        PopularReviewDto dto = new PopularReviewDto(
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

        PopularRankingSearchRequest request = new PopularRankingSearchRequest();
        request.setPeriod(RankingPeriod.DAILY);
        request.setDirection("DESC");
        request.setLimit(10);

        when(repository.findAllByPeriodWithCursor(eq(request), any()))
            .thenReturn(List.of(dto));

        // when
        CursorPageResponse<PopularReviewDto> result = service.getPopularReviews(request);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).bookTitle()).isEqualTo("이펙티브 자바");
    }
}