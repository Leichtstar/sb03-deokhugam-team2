package com.twogether.deokhugam.dashboard.controller;

import com.twogether.deokhugam.common.dto.CursorPageResponse;
import com.twogether.deokhugam.common.exception.DeokhugamException;
import com.twogether.deokhugam.common.exception.ErrorCode;
import com.twogether.deokhugam.dashboard.dto.response.PopularReviewDto;
import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import com.twogether.deokhugam.dashboard.service.PopularReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reviews/popular")
@RequiredArgsConstructor
public class PopularReviewController {

    private final PopularReviewService popularReviewService;

    @GetMapping
    @Operation(summary = "인기 리뷰 목록 조회", description = "기간별 인기 리뷰를 커서 기반으로 조회합니다.")
    public CursorPageResponse<PopularReviewDto> getPopularReviews(
        @RequestParam(name = "period", required = false, defaultValue = "DAILY")
        @Parameter(description = "랭킹 기간", example = "DAILY")
        String period,

        @RequestParam(name = "cursor", required = false)
        @Parameter(description = "커서 값 (id)", example = "7e8c7a32-f0cc-4f62-9b92-181aca8850d0")
        String cursor,

        @RequestParam(name = "after", required = false)
        @Parameter(description = "보조 커서 (createdAt)", example = "2025-07-15T18:20:30")
        String after,

        @RequestParam(name = "limit", required = false, defaultValue = "10")
        @Parameter(description = "페이지 크기", example = "10")
        int limit
    ) {
        RankingPeriod rankingPeriod;
        try {
            rankingPeriod = RankingPeriod.valueOf(period.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new DeokhugamException(ErrorCode.INVALID_RANKING_PERIOD);
        }

        return popularReviewService.getPopularReviews(rankingPeriod, cursor, after, limit);
    }
}