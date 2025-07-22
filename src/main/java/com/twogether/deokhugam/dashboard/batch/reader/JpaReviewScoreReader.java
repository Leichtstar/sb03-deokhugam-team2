package com.twogether.deokhugam.dashboard.batch.reader;

import com.twogether.deokhugam.dashboard.batch.model.ReviewScoreDto;
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
public class JpaReviewScoreReader implements ItemReader<ReviewScoreDto> {

    private final EntityManager entityManager;

    @Value("#{jobParameters['period']}")
    private String periodString;

    @Value("#{jobParameters['now']}")
    private String nowString;

    private Iterator<ReviewScoreDto> reviewIterator;

    @Override
    public ReviewScoreDto read() {
        if (reviewIterator == null) {
            reviewIterator = fetchReviewScores().iterator();
        }
        return reviewIterator.hasNext() ? reviewIterator.next() : null;
    }

    private List<ReviewScoreDto> fetchReviewScores() {
        RankingPeriod period = RankingPeriod.valueOf(periodString);
        LocalDateTime now = LocalDateTime.parse(nowString);

        Instant start = period.getStartTime(now).atZone(ZoneId.of("UTC")).toInstant();
        Instant end = period.getEndTime(now).atZone(ZoneId.of("UTC")).toInstant();

        return entityManager.createQuery("""
            SELECT r.id, u.id, u.nickname, r.content,
                   COALESCE(r.rating, 0.0),
                   b.id, b.title, b.thumbnailUrl,
                   COALESCE(r.likeCount, 0), COALESCE(r.commentCount, 0)
            FROM Review r
            JOIN r.user u
            JOIN r.book b
            WHERE r.createdAt >= :start AND r.createdAt < :end
              AND r.isDeleted = false
            ORDER BY COALESCE(r.likeCount, 0) DESC, COALESCE(r.commentCount, 0) DESC
        """, Object[].class)
            .setParameter("start", start)
            .setParameter("end", end)
            .setMaxResults(1000)
            .getResultList()
            .stream()
            .map(row -> new ReviewScoreDto(
                (UUID) row[0],
                (UUID) row[1],
                (String) row[2],
                (String) row[3],
                ((Number) row[4]).doubleValue(),
                (UUID) row[5],
                (String) row[6],
                (String) row[7],
                ((Number) row[8]).longValue(),
                ((Number) row[9]).longValue(),
                period
            ))
            .toList();
    }
}