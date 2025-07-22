package com.twogether.deokhugam.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "알림 확인 여부 수정 요청 DTO")
public record NotificationUpdateRequest(

    @NotNull
    @Schema(description = "알림 확인 여부")
    Boolean confirmed

) {}
