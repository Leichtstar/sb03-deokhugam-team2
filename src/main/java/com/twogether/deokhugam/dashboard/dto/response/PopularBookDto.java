package com.twogether.deokhugam.dashboard.dto.response;

import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Popular Book Response DTO")
public record PopularBookDto(

    @Schema(description = "Ranking ID")
    UUID id,

    @Schema(description = "Book ID")
    UUID bookId,

    @Schema(description = "Book title")
    String title,

    @Schema(description = "Author name")
    String author,

    @Schema(description = "Book thumbnail URL")
    String thumbnailUrl,

    @Schema(description = "Ranking period (DAILY, WEEKLY, MONTHLY, ALL_TIME)")
    RankingPeriod period,

    @Schema(description = "Book rank in the list")
    int rank,

    @Schema(description = "Computed popularity score")
    double score,

    @Schema(description = "Number of reviews for this book")
    long reviewCount,

    @Schema(description = "Average review rating")
    double rating,

    @Schema(description = "Timestamp when this ranking was recorded")
    LocalDateTime createdAt

) {}
