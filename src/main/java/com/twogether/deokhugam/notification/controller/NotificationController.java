package com.twogether.deokhugam.notification.controller;

import com.twogether.deokhugam.common.dto.CursorPageResponse;
import com.twogether.deokhugam.notification.dto.NotificationDto;
import com.twogether.deokhugam.notification.dto.NotificationUpdateRequest;
import com.twogether.deokhugam.notification.service.NotificationQueryService;
import com.twogether.deokhugam.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
public class NotificationController {

    private final NotificationQueryService notificationQueryService;
    private final NotificationService notificationService;

    @Operation(summary = "알림 목록 조회", description = "사용자의 알림 목록을 커서 기반으로 조회합니다. 최신 알림부터 정렬됩니다.")
    @GetMapping("/api/notifications")
    public CursorPageResponse<NotificationDto> getNotifications(
        @Parameter(description = "요청자 ID", required = true)
        @RequestHeader("Deokhugam-Request-User-ID") UUID userId,

        @Parameter(description = "커서 (이전 페이지 마지막 알림의 createdAt ISO-8601 형식)", example = "2025-07-20T19:30:00")
        @RequestParam(required = false) String cursor,

        @Parameter(description = "보조 커서 (정확한 정렬을 위한 createdAt 값)", example = "2025-07-20T19:00:00")
        @RequestParam(required = false) LocalDateTime after,

        @Min(value = 1, message = "limit은 1 이상의 정수")
        @Parameter(description = "페이지 크기", example = "20")
        @RequestParam(required = false) Integer limit,

        @Parameter(description = "정렬 방향 (DESC 또는 ASC)", example = "DESC")
        @RequestParam(required = false) Sort.Direction direction
    ) {
        return notificationQueryService.getNotifications(userId, cursor, after, limit, direction);
    }

    @Operation(summary = "알림 읽음 처리", description = "특정 알림의 확인 여부를 true 또는 false로 수정합니다.")
    @PatchMapping("/api/notifications/{notificationId}")
    public ResponseEntity<NotificationDto> updateNotification(
        @Parameter(description = "알림 ID", required = true)
        @PathVariable UUID notificationId,

        @Parameter(description = "요청자 ID", required = true)
        @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId,

        @Valid @RequestBody NotificationUpdateRequest request
    ) {
        NotificationDto updated = notificationService.updateConfirmedStatus(
            notificationId, requestUserId, request.confirmed()
        );
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "모든 알림 읽음 처리", description = "사용자의 모든 알림을 확인 처리합니다.")
    @PatchMapping("/api/notifications/read-all")
    public ResponseEntity<Void> markAllAsRead(
        @Parameter(description = "요청자 ID", required = true)
        @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId
    ) {
        notificationService.markAllAsRead(requestUserId);
        return ResponseEntity.noContent().build();
    }
}