package com.twogether.deokhugam.dashboard.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.twogether.deokhugam.common.exception.DeokhugamException;
import com.twogether.deokhugam.common.exception.ErrorCode;
import com.twogether.deokhugam.dashboard.dto.response.PopularReviewDto;
import com.twogether.deokhugam.dashboard.entity.PopularReviewRanking;
import com.twogether.deokhugam.dashboard.entity.QPopularReviewRanking;
import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import com.twogether.deokhugam.dashboard.mapper.PopularReviewDtoMapper;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PopularReviewRankingCustomRepositoryImpl implements PopularReviewRankingCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<PopularReviewDto> findByPeriodWithCursor(
        RankingPeriod period,
        String cursor,
        String after,
        int limit
    ) {
        QPopularReviewRanking r = QPopularReviewRanking.popularReviewRanking;

        List<PopularReviewRanking> rankings = queryFactory
            .selectFrom(r)
            .where(
                r.period.eq(period)
                    .and(applyCursorConditions(r, cursor, after))
            )
            .orderBy(r.createdAt.desc(), r.id.desc())
            .limit(limit)
            .fetch();

        return PopularReviewDtoMapper.toDtoList(rankings);
    }

    private BooleanExpression applyCursorConditions(QPopularReviewRanking r, String cursor, String after) {
        BooleanExpression condition = null;

        try {
            if (after != null && cursor != null) {
                LocalDateTime afterTime = LocalDateTime.parse(after);
                UUID cursorId = UUID.fromString(cursor);
                condition = r.createdAt.lt(afterTime)
                    .or(r.createdAt.eq(afterTime).and(r.id.lt(cursorId)));
            } else if (after != null) {
                LocalDateTime afterTime = LocalDateTime.parse(after);
                condition = r.createdAt.lt(afterTime);
            } else if (cursor != null) {
                UUID cursorId = UUID.fromString(cursor);
                condition = r.id.lt(cursorId);
            }
        } catch (IllegalArgumentException | DateTimeParseException e) {
            throw new DeokhugamException(ErrorCode.INVALID_CURSOR);
        }

        return condition;
    }
}