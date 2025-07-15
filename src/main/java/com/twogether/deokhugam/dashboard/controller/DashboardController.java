package com.twogether.deokhugam.dashboard.controller;

import com.twogether.deokhugam.common.dto.CursorPageResponse;
import com.twogether.deokhugam.dashboard.dto.request.PopularRankingSearchRequest;
import com.twogether.deokhugam.dashboard.dto.response.PopularBookDto;
import com.twogether.deokhugam.dashboard.service.DashboardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/popular")
    public CursorPageResponse<PopularBookDto> getPopularBooks(
        @Valid PopularRankingSearchRequest request
    ) {
        return dashboardService.getPopularBooks(request);
    }
}
