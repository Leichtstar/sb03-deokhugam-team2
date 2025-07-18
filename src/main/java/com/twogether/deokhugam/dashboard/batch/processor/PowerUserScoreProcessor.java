package com.twogether.deokhugam.dashboard.batch.processor;

import com.twogether.deokhugam.dashboard.batch.model.PowerUserScoreDto;
import com.twogether.deokhugam.dashboard.entity.PowerUserRanking;
import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import com.twogether.deokhugam.user.entity.User;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import org.springframework.batch.item.ItemProcessor;

public class PowerUserScoreProcessor implements ItemProcessor<PowerUserScoreDto, PowerUserRanking> {

    private final EntityManager em;
    private final RankingPeriod period;

    public PowerUserScoreProcessor(EntityManager em, RankingPeriod period) {
        if (em == null) throw new IllegalArgumentException("EntityManager는 null일 수 없습니다.");
        if (period == null) throw new IllegalArgumentException("RankingPeriod는 null일 수 없습니다.");
        this.em = em;
        this.period = period;
    }

    @Override
    public PowerUserRanking process(PowerUserScoreDto dto) {
        User user = em.find(User.class, dto.userId());
        if (user == null) return null;

        return PowerUserRanking.builder()
            .user(user)
            .nickname(dto.nickname())
            .period(period)
            .score(dto.calculateScore())
            .reviewScoreSum(dto.reviewScoreSum())
            .likeCount(dto.likeCount())
            .commentCount(dto.commentCount())
            .rank(0)
            .createdAt(LocalDateTime.now())
            .build();
    }
}