package com.twogether.deokhugam.dashboard.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.twogether.deokhugam.dashboard.dto.response.PopularReviewDto;
import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import com.twogether.deokhugam.dashboard.repository.PopularReviewRankingRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PopularReviewServiceTest {

    private final PopularReviewRankingRepository repository = mock(PopularReviewRankingRepository.class);
    private final PopularReviewService service = new PopularReviewService(repository);

    @Test
    @DisplayName("기간별 인기 리뷰 목록을 반환한다")
    void getPopularReviews_shouldReturnReviewsByPeriod() {
        // given
        RankingPeriod period = RankingPeriod.DAILY;
        when(repository.findByPeriodWithCursor(period, null, null, 10))
            .thenReturn(List.of(new PopularReviewDto(/* TODO: 테스트용 생성자 */)));

        // when
        var result = service.getPopularReviews(period, null, null, 10);

        // then
        assertThat(result.content()).isNotEmpty();
    }
}