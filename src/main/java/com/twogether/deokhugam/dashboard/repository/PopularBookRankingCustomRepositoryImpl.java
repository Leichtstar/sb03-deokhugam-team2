package com.twogether.deokhugam.dashboard.repository;

import static com.twogether.deokhugam.dashboard.entity.QPopularBookRanking.popularBookRanking;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.twogether.deokhugam.dashboard.dto.request.PopularRankingSearchRequest;
import com.twogether.deokhugam.dashboard.entity.PopularBookRanking;
import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PopularBookRankingCustomRepositoryImpl implements PopularBookRankingCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<PopularBookRanking> findAllByPeriodWithCursor(PopularRankingSearchRequest request,
        Pageable pageable) {
        return queryFactory
            .selectFrom(popularBookRanking)
            .where(
                popularBookRanking.period.eq(RankingPeriod.valueOf(request.getPeriod())),
                ltCursor(request.getCursor(), request.getAfter())
            )
            .orderBy(
                popularBookRanking.rank.asc(),
                popularBookRanking.createdAt.asc()
            )
            .limit(pageable.getPageSize())
            .fetch();
    }

    private com.querydsl.core.types.dsl.BooleanExpression ltCursor(Long cursor, LocalDateTime after) {
        if (cursor == null || after == null) {
            return null;
        }
        return popularBookRanking.rank.gt(cursor)
            .or(popularBookRanking.rank.eq(cursor.intValue())
                .and(popularBookRanking.createdAt.gt(after)));
    }
}
