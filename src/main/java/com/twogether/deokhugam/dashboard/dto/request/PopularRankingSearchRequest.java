package com.twogether.deokhugam.dashboard.dto.request;

import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
public class PopularRankingSearchRequest {

    @NotNull
    @Schema(
        description = "랭킹 기간 (DAILY, WEEKLY, MONTHLY, ALL_TIME)",
        example = "DAILY",
        defaultValue = "DAILY"
    )
    private RankingPeriod period = RankingPeriod.DAILY;

    @NotNull
    @Schema(
        description = "정렬 방향 (ASC 또는 DESC)",
        example = "ASC",
        defaultValue = "ASC"
    )
    private String direction = "ASC";

    @Schema(description = "정렬 기준 커서", example = "10")
    private String cursor;

    public Integer parseCursor() {
        try {
            return (cursor == null || cursor.isBlank()) ? null : Integer.parseInt(cursor);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Schema(
        description = "보조 커서(createdAt)",
        example = "2025-07-15T00:00:00"
    )
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime after;

    @Min(1)
    @Max(50)
    @Schema(
        description = "페이지 크기",
        example = "50",
        defaultValue = "50"
    )
    private int limit = 50;
}
