package com.twogether.deokhugam.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "알림 응답 DTO")
public record NotificationDto(

    @Schema(description = "알림 ID")
    UUID id,

    @Schema(description = "수신 사용자 ID")
    UUID userId,

    @Schema(description = "리뷰 ID")
    UUID reviewId,

    @Schema(description = "리뷰 제목 (도서 제목)")
    String reviewTitle,

    @Schema(description = "알림 내용")
    String content,

    @Schema(description = "알림 확인 여부")
    boolean confirmed,

    @Schema(description = "알림 생성 시각", format = "date-time")
    Instant createdAt,

    @Schema(description = "알림 수정 시각", format = "date-time")
    Instant updatedAt

) {}