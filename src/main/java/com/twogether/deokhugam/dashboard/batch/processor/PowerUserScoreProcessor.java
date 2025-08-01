package com.twogether.deokhugam.dashboard.batch.processor;

import com.twogether.deokhugam.dashboard.batch.model.PowerUserScoreDto;
import com.twogether.deokhugam.dashboard.entity.PowerUserRanking;
import com.twogether.deokhugam.user.entity.User;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;

@Slf4j
public class PowerUserScoreProcessor implements ItemProcessor<PowerUserScoreDto, PowerUserRanking> {

    private final Map<UUID, User> userMap;
    private final Instant executionTime;
    private final MeterRegistry meterRegistry;
    private final EntityManager em;

    public PowerUserScoreProcessor(Map<UUID, User> userMap,
        Instant executionTime,
        MeterRegistry meterRegistry,
        EntityManager em) {
        this.userMap = userMap;
        this.executionTime = executionTime;
        this.meterRegistry = meterRegistry;
        this.em = em;
    }

    @Override
    public PowerUserRanking process(PowerUserScoreDto dto) {
        Timer.Sample sample = Timer.start(meterRegistry);

        User user = userMap.get(dto.userId());
        if (user == null) {
            log.warn("PowerUserScoreProcessor: 유저 누락 → userId={}, nickname={}", dto.userId(), dto.nickname());
            return null;
        }

        double baseScore = dto.calculateScore();
        double freshnessBonus = calculateFreshnessBonus(dto.userId());
        double finalScore = baseScore + freshnessBonus;

        meterRegistry.counter("batch.power_user.processed.count").increment();
        sample.stop(Timer.builder("batch.power_user.processed.timer")
            .description("파워 유저 점수 계산 시간")
            .tag("userId", dto.userId().toString())
            .register(meterRegistry));

        return PowerUserRanking.builder()
            .user(user)
            .nickname(dto.nickname())
            .period(dto.period())
            .score(finalScore)
            .reviewScoreSum(dto.reviewScoreSum())
            .likeCount(dto.likeCount())
            .commentCount(dto.commentCount())
            .rank(0)
            .createdAt(executionTime)
            .build();
    }

    private double calculateFreshnessBonus(UUID userId) {
        Instant latestReviewTime = getMaxCreatedAt("""
            SELECT MAX(r.createdAt) FROM Review r
            WHERE r.user.id = :userId AND r.isDeleted = false
        """, userId);

        Instant latestCommentTime = getMaxCreatedAt("""
            SELECT MAX(c.createdAt) FROM Comment c
            WHERE c.user.id = :userId AND c.isDeleted = false
        """, userId);

        Instant latestActivity = maxInstant(latestReviewTime, latestCommentTime);
        return computeBonus(latestActivity);
    }

    private Instant getMaxCreatedAt(String jpql, UUID userId) {
        try {
            TypedQuery<Instant> query = em.createQuery(jpql, Instant.class);
            query.setParameter("userId", userId);
            return query.getSingleResult();
        } catch (Exception e) {
            log.warn("쿼리 실행 중 오류 발생: {}", jpql, e);
            return null;
        }
    }

    private Instant maxInstant(Instant a, Instant b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.isAfter(b) ? a : b;
    }

    private double computeBonus(Instant time) {
        if (time == null) return 0.0;
        long hoursAgo = Duration.between(time, Instant.now()).toHours();
        if (hoursAgo < 1) return 0.003;
        if (hoursAgo < 6) return 0.002;
        if (hoursAgo < 24) return 0.001;
        return 0.0;
    }
}