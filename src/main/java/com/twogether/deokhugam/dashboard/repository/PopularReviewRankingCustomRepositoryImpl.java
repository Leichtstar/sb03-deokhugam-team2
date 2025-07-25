package com.twogether.deokhugam.dashboard.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.twogether.deokhugam.common.exception.DeokhugamException;
import com.twogether.deokhugam.common.exception.ErrorCode;
import com.twogether.deokhugam.dashboard.dto.request.PopularRankingSearchRequest;
import com.twogether.deokhugam.dashboard.dto.response.PopularReviewDto;
import com.twogether.deokhugam.dashboard.dto.response.QPopularReviewDto;
import com.twogether.deokhugam.dashboard.entity.QPopularReviewRanking;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PopularReviewRankingCustomRepositoryImpl implements PopularReviewRankingCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<PopularReviewDto> findAllByPeriodWithCursor(PopularRankingSearchRequest request, Pageable pageable) {
        QPopularReviewRanking r = QPopularReviewRanking.popularReviewRanking;

        return queryFactory
            .select(new QPopularReviewDto(
                r.id,
                r.reviewId,
                r.bookId,
                r.bookTitle,
                r.bookThumbnailUrl,
                r.userId,
                r.userNickname,
                r.reviewContent,
                r.reviewRating,
                r.period,
                r.createdAt,
                r.rank,
                r.score,
                r.likeCount,
                r.commentCount
            ))
            .from(r)
            .where(
                r.period.eq(request.getPeriod()),
                ltCursor(request.getCursor(), request.getAfter(), request.getDirection())
            )
            .orderBy(getOrderSpecifiers(request.getDirection()))
            .limit(pageable.getPageSize())
            .fetch();
    }

    private BooleanExpression ltCursor(String cursor, Instant after, String direction) {
        QPopularReviewRanking r = QPopularReviewRanking.popularReviewRanking;
        if (cursor == null || after == null) return null;

        UUID cursorId;
        try {
            cursorId = UUID.fromString(cursor);
        } catch (IllegalArgumentException e) {
            throw new DeokhugamException(ErrorCode.INVALID_CURSOR);
        }

        if ("DESC".equalsIgnoreCase(direction)) {
            return r.createdAt.lt(after)
                .or(r.createdAt.eq(after).and(r.id.lt(cursorId)));
        }
        return r.createdAt.gt(after)
            .or(r.createdAt.eq(after).and(r.id.gt(cursorId)));
    }

    private OrderSpecifier<?>[] getOrderSpecifiers(String direction) {
        QPopularReviewRanking r = QPopularReviewRanking.popularReviewRanking;
        if ("DESC".equalsIgnoreCase(direction)) {
            return new OrderSpecifier[]{
                r.score.desc(),
                r.createdAt.desc(),
                r.id.desc()
            };
        }
        return new OrderSpecifier[]{
            r.score.asc(),
            r.createdAt.asc(),
            r.id.asc()
        };
    }
}