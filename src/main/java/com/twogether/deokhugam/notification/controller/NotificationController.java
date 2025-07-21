package com.twogether.deokhugam.notification.controller;

import com.twogether.deokhugam.common.dto.CursorPageResponse;
import com.twogether.deokhugam.notification.dto.NotificationDto;
import com.twogether.deokhugam.notification.service.NotificationQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationQueryService notificationQueryService;

    @Operation(summary = "알림 목록 조회", description = "사용자의 알림 목록을 커서 기반으로 조회합니다. 최신 알림부터 정렬됩니다.")
    @GetMapping("/api/notifications")
    public CursorPageResponse<NotificationDto> getNotifications(
        @Parameter(description = "요청자 ID", required = true)
        @RequestHeader("Deokhugam-Request-User-ID") UUID userId,

        @Parameter(description = "커서 (이전 페이지 마지막 알림의 createdAt ISO-8601 형식)", example = "2025-07-20T19:30:00")
        @RequestParam(required = false) String cursor,

        @Parameter(description = "보조 커서 (정확한 정렬을 위한 createdAt 값)", example = "2025-07-20T19:00:00")
        @RequestParam(required = false) LocalDateTime after,

        @Parameter(description = "페이지 크기", example = "20")
        @RequestParam(required = false) Integer limit,

        @Parameter(description = "정렬 방향 (DESC 또는 ASC)", example = "DESC")
        @RequestParam(required = false) Sort.Direction direction
    ) {
        return notificationQueryService.getNotifications(userId, cursor, after, limit, direction);
    }
}