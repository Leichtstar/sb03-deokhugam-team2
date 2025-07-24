package com.twogether.deokhugam.dashboard.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.twogether.deokhugam.dashboard.dto.request.PopularRankingSearchRequest;
import com.twogether.deokhugam.dashboard.dto.response.PowerUserDto;
import com.twogether.deokhugam.dashboard.dto.response.QPowerUserDto;
import com.twogether.deokhugam.dashboard.entity.QPowerUserRanking;
import com.twogether.deokhugam.user.entity.QUser;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PowerUserRankingCustomRepositoryImpl implements PowerUserRankingCustomRepository {

    private final JPAQueryFactory queryFactory;
    private static final int MAX_PAGE_SIZE = 50;

    @Override
    public List<PowerUserDto> findAllByPeriodWithCursor(PopularRankingSearchRequest request, Pageable pageable) {
        QPowerUserRanking r = QPowerUserRanking.powerUserRanking;
        QUser u = QUser.user;

        return queryFactory
            .select(new QPowerUserDto(
                r.user.id,
                r.nickname,
                r.period,
                r.createdAt,
                r.rank,
                r.score,
                r.reviewScoreSum,
                r.likeCount,
                r.commentCount
            ))
            .from(r)
            .where(
                r.period.eq(request.getPeriod()),
                ltCursor(request.parseCursor(), request.getAfter(), request.getDirection())
            )
            .orderBy(getOrderSpecifiers(request.getDirection()))
            .limit(Math.min(pageable.getPageSize(), MAX_PAGE_SIZE))
            .fetch();
    }

    private BooleanExpression ltCursor(Integer cursor, Instant after, String direction) {
        QPowerUserRanking r = QPowerUserRanking.powerUserRanking;
        if (cursor == null || after == null) return null;

        if ("DESC".equalsIgnoreCase(direction)) {
            return r.rank.lt(cursor)
                .or(r.rank.eq(cursor).and(r.createdAt.lt(after)));
        }
        return r.rank.gt(cursor)
            .or(r.rank.eq(cursor).and(r.createdAt.gt(after)));
    }

    private OrderSpecifier<?>[] getOrderSpecifiers(String direction) {
        QPowerUserRanking r = QPowerUserRanking.powerUserRanking;
        if ("DESC".equalsIgnoreCase(direction)) {
            return new OrderSpecifier[]{
                r.rank.desc(),
                r.createdAt.desc()
            };
        }
        return new OrderSpecifier[]{
            r.rank.asc(),
            r.createdAt.asc()
        };
    }
}