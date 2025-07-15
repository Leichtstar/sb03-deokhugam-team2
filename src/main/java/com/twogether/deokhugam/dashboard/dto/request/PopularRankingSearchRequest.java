package com.twogether.deokhugam.dashboard.dto.request;

import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
public class PopularRankingSearchRequest {

    @Schema(
        description = "랭킹 기간 (DAILY, WEEKLY, MONTHLY, ALL_TIME)",
        example = "DAILY",
        defaultValue = "DAILY"
    )
    private RankingPeriod period = RankingPeriod.DAILY;

    @Schema(
        description = "정렬 방향 (ASC 또는 DESC)",
        example = "ASC",
        defaultValue = "ASC"
    )
    private String direction = "ASC";

    @Schema(description = "정렬 기준 커서", example = "10")
    private String cursor;

    @Schema(
        description = "보조 커서(createdAt)",
        example = "2025-07-15T00:00:00"
    )
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime after;

    @Schema(
        description = "페이지 크기",
        example = "50",
        defaultValue = "50"
    )
    @Min(1)
    private int limit = 50;
}
