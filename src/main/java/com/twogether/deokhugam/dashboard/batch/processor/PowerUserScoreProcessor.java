package com.twogether.deokhugam.dashboard.batch.processor;

import com.twogether.deokhugam.dashboard.batch.model.PowerUserScoreDto;
import com.twogether.deokhugam.dashboard.entity.PowerUserRanking;
import com.twogether.deokhugam.user.entity.User;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import org.springframework.batch.item.ItemProcessor;

public class PowerUserScoreProcessor implements ItemProcessor<PowerUserScoreDto, PowerUserRanking> {

    private final EntityManager em;
    private final Instant executionTime;
    private final MeterRegistry meterRegistry;

    public PowerUserScoreProcessor(EntityManager em, Instant executionTime, MeterRegistry meterRegistry) {
        this.em = em;
        this.executionTime = executionTime;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public PowerUserRanking process(PowerUserScoreDto dto) {
        Timer.Sample sample = Timer.start(meterRegistry);

        User user = em.find(User.class, dto.userId());
        if (user == null) return null;

        double score = dto.calculateScore();

        // 커스텀 메트릭 - 계산 건수 카운터 증가
        meterRegistry.counter("batch.power_user.processed.count").increment();

        // 커스텀 메트릭 - 개별 처리 시간 기록
        sample.stop(Timer.builder("batch.power_user.processed.timer")
            .description("파워 유저 점수 계산에 소요된 시간")
            .tag("userId", dto.userId().toString())
            .register(meterRegistry));

        return PowerUserRanking.builder()
            .user(user)
            .nickname(dto.nickname())
            .period(dto.period())
            .score(score)
            .reviewScoreSum(dto.reviewScoreSum())
            .likeCount(dto.likeCount())
            .commentCount(dto.commentCount())
            .rank(0)
            .createdAt(executionTime)
            .build();
    }
}