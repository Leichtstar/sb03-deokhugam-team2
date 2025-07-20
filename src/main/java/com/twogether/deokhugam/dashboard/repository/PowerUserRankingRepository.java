package com.twogether.deokhugam.dashboard.repository;

import com.twogether.deokhugam.dashboard.entity.PowerUserRanking;
import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface PowerUserRankingRepository extends JpaRepository<PowerUserRanking, UUID>, PowerUserRankingCustomRepository {

    @Modifying
    @Query("DELETE FROM PowerUserRanking r WHERE r.period = :period")
    void deleteByPeriod(RankingPeriod period);

    List<PowerUserRanking> findAllByPeriod(RankingPeriod period);
}