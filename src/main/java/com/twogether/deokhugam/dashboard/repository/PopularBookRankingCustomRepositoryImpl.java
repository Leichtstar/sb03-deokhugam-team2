package com.twogether.deokhugam.dashboard.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.twogether.deokhugam.book.entity.QBook;
import com.twogether.deokhugam.dashboard.dto.request.PopularRankingSearchRequest;
import com.twogether.deokhugam.dashboard.dto.response.PopularBookDto;
import com.twogether.deokhugam.dashboard.dto.response.QPopularBookDto;
import com.twogether.deokhugam.dashboard.entity.QPopularBookRanking;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PopularBookRankingCustomRepositoryImpl implements PopularBookRankingCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<PopularBookDto> findAllByPeriodWithCursor(PopularRankingSearchRequest request, Pageable pageable) {
        QPopularBookRanking r = QPopularBookRanking.popularBookRanking;
        QBook b = QBook.book;

        return queryFactory
            .select(new QPopularBookDto(
                r.id,
                b.id,
                r.title,
                r.author,
                r.thumbnailUrl,
                r.period,
                r.rank,
                r.score,
                r.reviewCount,
                r.rating,
                r.createdAt
            ))
            .from(r)
            .join(r.book, b)
            .where(
                r.period.eq(request.getPeriod()),
                ltCursor(request.parseCursor(), request.getAfter(), request.getDirection())
            )
            .orderBy(getOrderSpecifiers(request.getDirection()))
            .limit(pageable.getPageSize())
            .fetch();
    }

    private BooleanExpression ltCursor(Integer cursor, Instant after, String direction) {
        QPopularBookRanking r = QPopularBookRanking.popularBookRanking;
        if (cursor == null || after == null) return null;

        if ("DESC".equalsIgnoreCase(direction)) {
            return r.rank.lt(cursor)
                .or(r.rank.eq(cursor).and(r.createdAt.lt(after)));
        }
        return r.rank.gt(cursor)
            .or(r.rank.eq(cursor).and(r.createdAt.gt(after)));
    }

    private OrderSpecifier<?>[] getOrderSpecifiers(String direction) {
        QPopularBookRanking r = QPopularBookRanking.popularBookRanking;
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
