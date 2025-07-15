package com.twogether.deokhugam.dashboard.dto.response;

import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "인기 도서 응답 DTO")
public record PopularBookDto(

    @Schema(description = "인기 도서 랭킹 ID")
    UUID id,

    @Schema(description = "도서 ID")
    UUID bookId,

    @Schema(description = "도서 제목")
    String title,

    @Schema(description = "저자명")
    String author,

    @Schema(description = "도서 썸네일 이미지 URL")
    String thumbnailUrl,

    @Schema(description = "랭킹 기간 (DAILY, WEEKLY, MONTHLY, ALL_TIME)")
    RankingPeriod period,

    @Schema(description = "랭킹 순위")
    int rank,

    @Schema(description = "인기 점수")
    double score,

    @Schema(description = "리뷰 수")
    long reviewCount,

    @Schema(description = "평점")
    double rating,

    @Schema(description = "랭킹 생성 시각")
    LocalDateTime createdAt

) {}
