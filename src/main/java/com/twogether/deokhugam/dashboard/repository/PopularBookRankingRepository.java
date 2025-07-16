package com.twogether.deokhugam.dashboard.repository;

import com.twogether.deokhugam.dashboard.entity.PopularBookRanking;
import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import jakarta.transaction.Transactional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface PopularBookRankingRepository extends JpaRepository<PopularBookRanking, UUID>, PopularBookRankingCustomRepository {

    @Modifying
    @Transactional
    @Query("DELETE FROM PopularBookRanking r WHERE r.period = :period")
    void deleteByPeriod(RankingPeriod period);
}
