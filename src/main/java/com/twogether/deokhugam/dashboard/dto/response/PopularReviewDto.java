package com.twogether.deokhugam.dashboard.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "인기 리뷰 응답 DTO")
public record PopularReviewDto(

    @Schema(description = "인기 리뷰 랭킹 ID")
    UUID id,

    @Schema(description = "리뷰 ID")
    UUID reviewId,

    @Schema(description = "도서 ID")
    UUID bookId,

    @Schema(description = "도서 제목")
    String bookTitle,

    @Schema(description = "도서 썸네일 이미지 URL")
    String bookThumbnailUrl,

    @Schema(description = "사용자 ID")
    UUID userId,

    @Schema(description = "사용자 닉네임")
    String userNickname,

    @Schema(description = "리뷰 내용")
    String reviewContent,

    @Schema(description = "리뷰 평점")
    double reviewRating,

    @Schema(description = "랭킹 기간 (DAILY, WEEKLY, MONTHLY, ALL_TIME)")
    RankingPeriod period,

    @Schema(description = "랭킹 생성 시각")
    LocalDateTime createdAt,

    @Schema(description = "랭킹 순위")
    int rank,

    @Schema(description = "인기 점수")
    double score,

    @Schema(description = "좋아요 수")
    long likeCount,

    @Schema(description = "댓글 수")
    long commentCount

) {

    @QueryProjection
    public PopularReviewDto(
        UUID id,
        UUID reviewId,
        UUID bookId,
        String bookTitle,
        String bookThumbnailUrl,
        UUID userId,
        String userNickname,
        String reviewContent,
        double reviewRating,
        RankingPeriod period,
        LocalDateTime createdAt,
        int rank,
        double score,
        long likeCount,
        long commentCount
    ) {
        this.id = id;
        this.reviewId = reviewId;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.bookThumbnailUrl = bookThumbnailUrl;
        this.userId = userId;
        this.userNickname = userNickname;
        this.reviewContent = reviewContent;
        this.reviewRating = reviewRating;
        this.period = period;
        this.createdAt = createdAt;
        this.rank = rank;
        this.score = score;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
    }
}