package com.twogether.deokhugam.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Notification update request DTO")
public record NotificationUpdateRequest(

    @NotNull
    @Schema(description = "Confirmed status")
    Boolean confirmed

) {}
