package com.twogether.deokhugam.dashboard.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;

@Schema(description = "인기 도서 응답 DTO")
public record PopularBookDto(@Schema(description = "인기 도서 랭킹 ID") UUID id,
                             @Schema(description = "도서 ID") UUID bookId,
                             @Schema(description = "도서 제목") String title,
                             @Schema(description = "저자명") String author,
                             @Schema(description = "도서 썸네일 이미지 URL") String thumbnailUrl,
                             @Schema(description = "랭킹 기간 (DAILY, WEEKLY, MONTHLY, ALL_TIME)") RankingPeriod period,
                             @Schema(description = "랭킹 순위") int rank,
                             @Schema(description = "인기 점수") double score,
                             @Schema(description = "리뷰 수") long reviewCount,
                             @Schema(description = "평점") double rating,
                             @Schema(description = "랭킹 생성 시각") LocalDateTime createdAt) {

    @QueryProjection
    public PopularBookDto(UUID id, UUID bookId, String title, String author,
        String thumbnailUrl, RankingPeriod period, int rank,
        double score, long reviewCount, double rating,
        LocalDateTime createdAt) {
        this.id = id;
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.thumbnailUrl = thumbnailUrl;
        this.period = period;
        this.rank = rank;
        this.score = score;
        this.reviewCount = reviewCount;
        this.rating = rating;
        this.createdAt = createdAt;
    }
}