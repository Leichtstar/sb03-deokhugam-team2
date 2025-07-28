package com.twogether.deokhugam.dashboard.controller;

import com.twogether.deokhugam.common.dto.CursorPageResponse;
import com.twogether.deokhugam.dashboard.dto.request.PopularRankingSearchRequest;
import com.twogether.deokhugam.dashboard.dto.response.PowerUserDto;
import com.twogether.deokhugam.dashboard.service.PowerUserService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@RestController
@RequiredArgsConstructor
public class PowerUserController {

    private final PowerUserService powerUserService;

    @Operation(summary = "파워 유저 랭킹 조회", description = "기간별 파워 유저 목록을 커서 페이지네이션 방식으로 조회합니다.")
    @GetMapping("/power")
    public CursorPageResponse<PowerUserDto> getPowerUsers(
        @Valid PopularRankingSearchRequest request
    ) {
        return powerUserService.getPowerUsers(request);
    }
}