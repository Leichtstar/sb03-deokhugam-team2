package com.twogether.deokhugam.dashboard.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.twogether.deokhugam.dashboard.dto.response.PopularReviewDto;
import com.twogether.deokhugam.dashboard.entity.QPopularReviewRanking;
import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import java.util.List;
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

        return queryFactory
            .select(new QPopularReviewDto(
                r.id,
                r.review.id,
                r.book.id,
                r.book.title,
                r.book.thumbnailUrl,
                r.user.id,
                r.user.nickname,
                r.review.content,
                r.review.rating,
                r.period,
                r.createdAt,
                r.rank,
                r.score,
                r.likeCount,
                r.commentCount
            ))
            .from(r)
            .where(r.period.eq(period))
            .orderBy(r.createdAt.desc(), r.id.desc())
            .limit(limit)
            .fetch();
    }
}