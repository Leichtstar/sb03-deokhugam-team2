package com.twogether.deokhugam.dashboard.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "파워 유저 응답 DTO")
public record PowerUserDto(

    @Schema(description = "사용자 ID")
    UUID userId,

    @Schema(description = "사용자 닉네임")
    String nickname,

    @Schema(description = "랭킹 기간")
    RankingPeriod period,

    @Schema(description = "랭킹 생성 시각")
    LocalDateTime createdAt,

    @Schema(description = "순위")
    int rank,

    @Schema(description = "총 활동 점수")
    double score,

    @Schema(description = "작성한 리뷰의 총 인기 점수")
    double reviewScoreSum,

    @Schema(description = "좋아요 수")
    long likeCount,

    @Schema(description = "댓글 수")
    long commentCount

) {

    @QueryProjection
    public PowerUserDto(UUID userId, String nickname, RankingPeriod period, LocalDateTime createdAt, int rank,
        double score, double reviewScoreSum, long likeCount, long commentCount) {
        this.userId = userId;
        this.nickname = nickname;
        this.period = period;
        this.createdAt = createdAt;
        this.rank = rank;
        this.score = score;
        this.reviewScoreSum = reviewScoreSum;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
    }
}