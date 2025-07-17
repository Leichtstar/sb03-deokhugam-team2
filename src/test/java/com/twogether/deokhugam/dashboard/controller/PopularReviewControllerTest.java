package com.twogether.deokhugam.dashboard.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.twogether.deokhugam.common.dto.CursorPageResponse;
import com.twogether.deokhugam.common.exception.DeokhugamException;
import com.twogether.deokhugam.common.exception.ErrorCode;
import com.twogether.deokhugam.dashboard.dto.response.PopularReviewDto;
import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import com.twogether.deokhugam.dashboard.service.PopularReviewService;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PopularReviewController.class)
class PopularReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PopularReviewService popularReviewService;

    @Test
    @DisplayName("인기 리뷰 목록을 정상적으로 조회할 수 있다")
    void getPopularReviews_success() throws Exception {
        PopularReviewDto dummy = new PopularReviewDto(
            UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
            "Clean Code", "https://url",
            UUID.randomUUID(), "케빈",
            "최고의 개발 서적!", 5.0,
            RankingPeriod.DAILY, LocalDateTime.now(),
            1, 100.0, 20, 4
        );

        CursorPageResponse<PopularReviewDto> response = new CursorPageResponse<>(
            Collections.singletonList(dummy), dummy.id().toString(), dummy.createdAt(), 10, false
        );

        Mockito.when(popularReviewService.getPopularReviews(any(), any(), any(), any()))
            .thenReturn(response);

        mockMvc.perform(get("/api/reviews/popular")
                .param("period", "DAILY")
                .param("limit", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].bookTitle").value("Clean Code"))
            .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    @DisplayName("잘못된 랭킹 기간이면 400을 반환한다")
    void getPopularReviews_invalidPeriod() throws Exception {
        Mockito.when(popularReviewService.getPopularReviews(any(), any(), any(), any()))
            .thenThrow(new DeokhugamException(ErrorCode.INVALID_RANKING_PERIOD));

        mockMvc.perform(get("/api/reviews/popular")
                .param("period", "WRONG")
                .param("limit", "10"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_RANKING_PERIOD"));
    }
}