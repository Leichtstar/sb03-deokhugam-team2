package com.twogether.deokhugam.dashboard.batch.processor;

import com.twogether.deokhugam.dashboard.batch.model.PowerUserScoreDto;
import com.twogether.deokhugam.dashboard.entity.PowerUserRanking;
import com.twogether.deokhugam.user.entity.User;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import org.springframework.batch.item.ItemProcessor;

public class PowerUserScoreProcessor implements ItemProcessor<PowerUserScoreDto, PowerUserRanking> {

    private final EntityManager em;

    public PowerUserScoreProcessor(EntityManager em) {
        this.em = em;
    }

    @Override
    public PowerUserRanking process(PowerUserScoreDto dto) {
        User user = em.find(User.class, dto.userId());
        if (user == null) return null;

        return PowerUserRanking.builder()
            .user(user)
            .nickname(dto.nickname())
            .period(dto.period())
            .score(dto.calculateScore())
            .reviewScoreSum(dto.reviewScoreSum())
            .likeCount(dto.likeCount())
            .commentCount(dto.commentCount())
            .rank(0)
            .createdAt(Instant.now())
            .build();
    }
}