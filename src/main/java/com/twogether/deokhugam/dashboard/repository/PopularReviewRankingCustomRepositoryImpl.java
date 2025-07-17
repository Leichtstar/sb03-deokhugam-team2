package com.twogether.deokhugam.dashboard.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.twogether.deokhugam.dashboard.dto.response.PopularReviewDto;
import com.twogether.deokhugam.dashboard.entity.PopularReviewRanking;
import com.twogether.deokhugam.dashboard.entity.QPopularReviewRanking;
import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import com.twogether.deokhugam.dashboard.mapper.PopularReviewDtoMapper;
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

        List<PopularReviewRanking> rankings = queryFactory
            .selectFrom(r)
            .where(r.period.eq(period))
            .orderBy(r.createdAt.desc(), r.id.desc())
            .limit(limit)
            .fetch();

        return PopularReviewDtoMapper.toDtoList(rankings);
    }
}