package com.twogether.deokhugam.dashboard.dto.response;

import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Power User Response DTO")
public record PowerUserDto(

    @Schema(description = "User ID")
    UUID userId,

    @Schema(description = "User nickname")
    String nickname,

    @Schema(description = "Ranking period")
    RankingPeriod period,

    @Schema(description = "Timestamp when this ranking was recorded")
    LocalDateTime createdAt,

    @Schema(description = "Rank in the list")
    int rank,

    @Schema(description = "Total activity score")
    double score,

    @Schema(description = "Total review score sum")
    double reviewScoreSum,

    @Schema(description = "Number of likes")
    long likeCount,

    @Schema(description = "Number of comments")
    long commentCount

) {}