package com.twogether.deokhugam.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Notification response DTO")
public record NotificationDto(

    @Schema(description = "Notification ID")
    UUID id,

    @Schema(description = "User ID")
    UUID userId,

    @Schema(description = "Review ID")
    UUID reviewId,

    @Schema(description = "Review content")
    String reviewTitle,

    @Schema(description = "Notification content")
    String content,

    @Schema(description = "Confirmed status")
    boolean confirmed,

    @Schema(description = "Created time")
    LocalDateTime createdAt,

    @Schema(description = "Updated time")
    LocalDateTime updatedAt

) {}