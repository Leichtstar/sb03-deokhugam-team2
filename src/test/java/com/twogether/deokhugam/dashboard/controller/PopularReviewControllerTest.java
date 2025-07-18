//package com.twogether.deokhugam.dashboard.controller;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyInt;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//import com.twogether.deokhugam.common.dto.CursorPageResponse;
//import com.twogether.deokhugam.common.exception.DeokhugamException;
//import com.twogether.deokhugam.common.exception.ErrorCode;
//import com.twogether.deokhugam.dashboard.dto.response.PopularReviewDto;
//import java.time.LocalDateTime;
//import java.util.Collections;
//import org.hamcrest.Matchers;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.test.web.servlet.MockMvc;
//
//@WebMvcTest(PopularReviewController.class)
//class PopularReviewControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockitoBean
//    private PopularReviewService popularReviewService;
//
//    @Test
//    @DisplayName("인기 리뷰 목록을 정상적으로 조회할 수 있다")
//    void getPopularReviews_success() throws Exception {
//        CursorPageResponse<PopularReviewDto> dummyResponse = new CursorPageResponse<>(
//            Collections.emptyList(),
//            "dummy-cursor",
//            LocalDateTime.now(),
//            10,
//            false
//        );
//
//        Mockito.when(popularReviewService.getPopularReviews(
//            any(), anyString(), anyString(), anyInt()
//        )).thenReturn(dummyResponse);
//
//        mockMvc.perform(get("/api/reviews/popular")
//                .param("period", "DAILY")
//                .param("cursor", "someId")
//                .param("after", "2025-07-17T01:00:00")
//                .param("limit", "10")
//            )
//            .andExpect(status().isOk())
//            .andExpect(jsonPath("$.content").isArray())
//            .andExpect(jsonPath("$.hasNext").value(false));
//    }
//
//    @Test
//    @DisplayName("잘못된 랭킹 기간이면 400을 반환한다")
//    void getPopularReviews_invalidPeriod() throws Exception {
//        mockMvc.perform(get("/api/reviews/popular")
//                .param("period", "INVALID")
//                .param("limit", "10"))
//            .andExpect(status().isBadRequest())
//            .andExpect(jsonPath("$.code").value("INVALID_RANKING_PERIOD"));
//    }
//
//    @Test
//    @DisplayName("조회 결과가 없을 경우 빈 content와 hasNext=false를 반환한다")
//    void getPopularReviews_emptyResult() throws Exception {
//        CursorPageResponse<PopularReviewDto> emptyResponse = new CursorPageResponse<>(
//            Collections.emptyList(),
//            "cursor-empty",
//            LocalDateTime.now(),
//            10,
//            false
//        );
//
//        Mockito.when(popularReviewService.getPopularReviews(
//            any(), anyString(), anyString(), anyInt()
//        )).thenReturn(emptyResponse);
//
//        mockMvc.perform(get("/api/reviews/popular")
//                .param("period", "DAILY")
//                .param("limit", "10"))
//            .andExpect(status().isOk())
//            .andExpect(jsonPath("$.content", Matchers.hasSize(0)))
//            .andExpect(jsonPath("$.hasNext").value(false));
//    }
//
//    @Test
//    @DisplayName("잘못된 커서/after 값을 전달하면 INVALID_CURSOR 에러를 반환한다")
//    void getPopularReviews_invalidCursor() throws Exception {
//        Mockito.when(popularReviewService.getPopularReviews(
//            any(), anyString(), anyString(), anyInt()
//        )).thenThrow(new DeokhugamException(ErrorCode.INVALID_CURSOR));
//
//        mockMvc.perform(get("/api/reviews/popular")
//                .param("period", "DAILY")
//                .param("cursor", "invalidCursor")
//                .param("after", "invalidDate")
//                .param("limit", "10"))
//            .andExpect(status().isBadRequest())
//            .andExpect(jsonPath("$.code").value("INVALID_CURSOR"));
//    }
//}