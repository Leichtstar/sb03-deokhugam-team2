package com.twogether.deokhugam.dashboard.batch.reader;

import com.twogether.deokhugam.dashboard.batch.model.PowerUserScoreDto;
import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@StepScope
@Component
@RequiredArgsConstructor
public class JpaPowerUserScoreReader implements ItemReader<PowerUserScoreDto> {

    private final EntityManager entityManager;

    @Value("#{jobParameters['period']}")
    private String periodString;

    @Value("#{jobParameters['now']}")
    private String nowString;

    private Iterator<PowerUserScoreDto> iterator;

    @Override
    public PowerUserScoreDto read() {
        if (iterator == null) {
            iterator = fetchPowerUserScores().iterator();
        }
        return iterator.hasNext() ? iterator.next() : null;
    }

    private List<PowerUserScoreDto> fetchPowerUserScores() {
        RankingPeriod period = RankingPeriod.valueOf(periodString);
        LocalDateTime now = LocalDateTime.parse(nowString);

        Instant start = period.getStartTime(now).atZone(ZoneId.of("UTC")).toInstant();
        Instant end = period.getEndTime(now).atZone(ZoneId.of("UTC")).toInstant();

        return entityManager.createQuery("""
            SELECT u.id, u.nickname,
                   COALESCE(SUM(COALESCE(r.likeCount, 0) * 0.3 + COALESCE(r.commentCount, 0) * 0.7), 0.0),
                   COALESCE(SUM(COALESCE(r.likeCount, 0)), 0),
                   COALESCE(SUM(COALESCE(r.commentCount, 0)), 0)
            FROM Review r
            JOIN r.user u
            WHERE r.createdAt >= :start AND r.createdAt < :end
              AND r.isDeleted = false
            GROUP BY u.id, u.nickname
            ORDER BY 3 DESC
        """, Object[].class)
            .setParameter("start", start)
            .setParameter("end", end)
            .setMaxResults(1000)
            .getResultList()
            .stream()
            .map(row -> new PowerUserScoreDto(
                (UUID) row[0],
                (String) row[1],
                ((Number) row[2]).doubleValue(),
                ((Number) row[3]).longValue(),
                ((Number) row[4]).longValue(),
                period
            ))
            .toList();
    }
}