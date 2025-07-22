package com.twogether.deokhugam.dashboard.batch.reader;

import com.twogether.deokhugam.dashboard.batch.model.PowerUserScoreDto;
import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.support.IteratorItemReader;
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
        LocalDateTime start = period.getStartTime(now);
        LocalDateTime end = period.getEndTime(now);

        Instant startInstant = start.atZone(ZoneId.of("UTC")).toInstant();
        Instant endInstant = end.atZone(ZoneId.of("UTC")).toInstant();

        return entityManager.createQuery("""
            SELECT r.user.id, r.userNickName,
                   SUM(COALESCE(r.likeCount, 0) * 0.3 + COALESCE(r.commentCount, 0) * 0.7),
                   SUM(COALESCE(r.likeCount, 0)),
                   SUM(COALESCE(r.commentCount, 0))
            FROM Review r
            WHERE r.createdAt >= :start AND r.createdAt < :end
              AND r.isDeleted = false
            GROUP BY r.user.id, r.userNickName
            ORDER BY 3 DESC
        """, Object[].class)
            .setParameter("start", startInstant)
            .setParameter("end", endInstant)
            .setMaxResults(1000)
            .getResultList()
            .stream()
            .map(row -> new PowerUserScoreDto(
                (UUID) row[0], (String) row[1], (Double) row[2],
                (Long) row[3], (Long) row[4], period
            ))
            .toList();
    }
}