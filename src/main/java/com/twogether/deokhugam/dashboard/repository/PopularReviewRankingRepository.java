package com.twogether.deokhugam.dashboard.repository;

import com.twogether.deokhugam.dashboard.entity.PopularReviewRanking;
import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PopularReviewRankingRepository extends JpaRepository<PopularReviewRanking, UUID>, PopularReviewRankingCustomRepository {

    List<PopularReviewRanking> findByPeriodOrderByRankAsc(RankingPeriod period);
}