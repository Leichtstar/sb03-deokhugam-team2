package com.twogether.deokhugam.dashboard.controller;

import com.twogether.deokhugam.common.dto.CursorPageResponse;
import com.twogether.deokhugam.dashboard.dto.request.PopularRankingSearchRequest;
import com.twogether.deokhugam.dashboard.dto.response.PopularBookDto;
import com.twogether.deokhugam.dashboard.dto.response.PopularReviewDto;
import com.twogether.deokhugam.dashboard.service.PopularBookService;
import com.twogether.deokhugam.dashboard.service.PopularReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DashboardController {

    private final PopularBookService popularBookService;
    private final PopularReviewService popularReviewService;

    @GetMapping("/books/popular")
    public CursorPageResponse<PopularBookDto> getPopularBooks(
        @Valid PopularRankingSearchRequest request
    ) {
        return popularBookService.getPopularBooks(request);
    }

    @GetMapping("/reviews/popular")
    public CursorPageResponse<PopularReviewDto> getPopularReviews(
        @Valid PopularRankingSearchRequest request
    ) {
        return popularReviewService.getPopularReviews(request);
    }
}
