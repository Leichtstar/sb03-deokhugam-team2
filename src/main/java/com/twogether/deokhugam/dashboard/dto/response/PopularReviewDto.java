package com.twogether.deokhugam.dashboard.dto.response;

import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Popular Review Response DTO")
public record PopularReviewDto(

    @Schema(description = "Ranking ID")
    UUID id,

    @Schema(description = "Review ID")
    UUID reviewId,

    @Schema(description = "Book ID")
    UUID bookId,

    @Schema(description = "Book title")
    String bookTitle,

    @Schema(description = "Book thumbnail URL")
    String bookThumbnailUrl,

    @Schema(description = "User ID")
    UUID userId,

    @Schema(description = "User nickname")
    String userNickname,

    @Schema(description = "Review content")
    String reviewContent,

    @Schema(description = "Review rating")
    double reviewRating,

    @Schema(description = "Ranking period")
    RankingPeriod period,

    @Schema(description = "Timestamp")
    LocalDateTime createdAt,

    @Schema(description = "Review rank")
    int rank,

    @Schema(description = "Popularity score")
    double score,

    @Schema(description = "Number of likes")
    long likeCount,

    @Schema(description = "Number of comments")
    long commentCount

) {}