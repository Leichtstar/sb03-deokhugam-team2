package com.twogether.deokhugam.dashboard.repository;

import com.twogether.deokhugam.dashboard.entity.PopularReviewRanking;
import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface PopularReviewRankingRepository extends JpaRepository<PopularReviewRanking, UUID>, PopularReviewRankingCustomRepository {

    @Modifying
    @Transactional
    @Query("DELETE FROM PopularReviewRanking r WHERE r.period = :period")
    void deleteByPeriod(RankingPeriod period);

    List<PopularReviewRanking> findByPeriodOrderByRankAsc(RankingPeriod period);
}