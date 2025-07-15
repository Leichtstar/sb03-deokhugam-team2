package com.twogether.deokhugam.dashboard.repository;

import static com.twogether.deokhugam.dashboard.entity.QPopularBookRanking.popularBookRanking;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.twogether.deokhugam.dashboard.dto.request.PopularRankingSearchRequest;
import com.twogether.deokhugam.dashboard.entity.PopularBookRanking;
import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PopularBookRankingCustomRepositoryImpl implements PopularBookRankingCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<PopularBookRanking> findAllByPeriodWithCursor(PopularRankingSearchRequest request, Pageable pageable) {
        RankingPeriod period = request.getPeriod();
        Integer parsedCursor = parseCursor(request.getCursor());
        LocalDateTime after = request.getAfter();

        return queryFactory
            .selectFrom(popularBookRanking)
            .where(
                popularBookRanking.period.eq(period),
                ltCursor(parsedCursor, after)
            )
            .orderBy(
                popularBookRanking.rank.asc(),
                popularBookRanking.createdAt.asc()
            )
            .limit(pageable.getPageSize())
            .fetch();
    }

    private BooleanExpression ltCursor(Integer cursor, LocalDateTime after) {
        if (cursor == null || after == null) {
            return null;
        }

        return popularBookRanking.rank.gt(cursor)
            .or(popularBookRanking.rank.eq(cursor)
                .and(popularBookRanking.createdAt.gt(after)));
    }

    private Integer parseCursor(String cursorStr) {
        if (cursorStr == null || cursorStr.isBlank()) return null;
        try {
            return Integer.parseInt(cursorStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("유효하지 않은 커서 값입니다: " + cursorStr);
        }
    }
}
