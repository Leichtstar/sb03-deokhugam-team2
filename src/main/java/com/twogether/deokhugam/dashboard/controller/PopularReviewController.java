package com.twogether.deokhugam.dashboard.controller;

import com.twogether.deokhugam.common.dto.CursorPageResponse;
import com.twogether.deokhugam.dashboard.dto.request.PopularRankingSearchRequest;
import com.twogether.deokhugam.dashboard.dto.response.PopularReviewDto;
import com.twogether.deokhugam.dashboard.service.PopularReviewService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class PopularReviewController {

    private final PopularReviewService popularReviewService;

    @Operation(summary = "인기 리뷰 랭킹 조회", description = "기간별 인기 리뷰 목록을 커서 페이지네이션 방식으로 조회합니다.")
    @GetMapping("/popular")
    public CursorPageResponse<PopularReviewDto> getPopularReviews(
        @Valid PopularRankingSearchRequest request
    ) {
        return popularReviewService.getPopularReviews(request);
    }
}