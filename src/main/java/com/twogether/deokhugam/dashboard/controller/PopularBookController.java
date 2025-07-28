package com.twogether.deokhugam.dashboard.controller;

import com.twogether.deokhugam.common.dto.CursorPageResponse;
import com.twogether.deokhugam.dashboard.dto.request.PopularRankingSearchRequest;
import com.twogether.deokhugam.dashboard.dto.response.PopularBookDto;
import com.twogether.deokhugam.dashboard.service.PopularBookService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@RestController
@RequiredArgsConstructor
public class PopularBookController {

    private final PopularBookService popularBookService;

    @Operation(summary = "인기 도서 랭킹 조회", description = "기간별 인기 도서 목록을 커서 페이지네이션 방식으로 조회합니다.")
    @GetMapping("/popular")
    public CursorPageResponse<PopularBookDto> getPopularBooks(
        @Valid PopularRankingSearchRequest request
    ) {
        return popularBookService.getPopularBooks(request);
    }
}