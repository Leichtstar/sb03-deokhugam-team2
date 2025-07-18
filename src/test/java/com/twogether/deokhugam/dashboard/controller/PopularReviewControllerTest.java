package com.twogether.deokhugam.dashboard.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.twogether.deokhugam.common.dto.CursorPageResponse;
import com.twogether.deokhugam.common.exception.DeokhugamException;
import com.twogether.deokhugam.common.exception.ErrorCode;
import com.twogether.deokhugam.dashboard.dto.response.PopularReviewDto;
import com.twogether.deokhugam.dashboard.service.PopularBookService;
import com.twogether.deokhugam.dashboard.service.PopularReviewService;
import java.util.Collections;
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
    private PopularBookService popularBookService;

    @MockitoBean
    private PopularReviewService popularReviewService;

    @Test
    @DisplayName("인기 리뷰 목록을 정상적으로 조회할 수 있다")
    void getPopularReviews_success() throws Exception {
        // given
        CursorPageResponse<PopularReviewDto> dummyResponse = new CursorPageResponse<>(
            Collections.emptyList(),
            null,
            null,
            10,
            false
        );

        Mockito.when(popularReviewService.getPopularReviews(any()))
            .thenReturn(dummyResponse);

        // when & then
        mockMvc.perform(get("/api/reviews/popular")
                .param("period", "DAILY")
                .param("direction", "ASC")
                .param("limit", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    @DisplayName("잘못된 정렬 방향이면 400을 반환한다")
    void getPopularReviews_invalidDirection() throws Exception {
        Mockito.when(popularReviewService.getPopularReviews(any()))
            .thenThrow(new DeokhugamException(ErrorCode.INVALID_DIRECTION));

        mockMvc.perform(get("/api/reviews/popular")
                .param("period", "DAILY")
                .param("direction", "WRONG")
                .param("limit", "10"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_DIRECTION"));
    }
}