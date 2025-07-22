package com.twogether.deokhugam.dashboard.batch.reader;

import com.twogether.deokhugam.dashboard.batch.model.ReviewScoreDto;
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
        RankingPeriod period;
        LocalDateTime now;
        try {
            period = RankingPeriod.valueOf(periodString);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("잘못된 RankingPeriod 값입니다: " + periodString, e);
        }
        try {
            now = LocalDateTime.parse(nowString);
        } catch (Exception e) {
            throw new IllegalArgumentException("잘못된 LocalDateTime 형식입니다: " + nowString, e);
        }

        LocalDateTime start = period.getStartTime(now);
        LocalDateTime end = period.getEndTime(now);

        Instant startInstant = start.atZone(ZoneId.of("UTC")).toInstant();
        Instant endInstant = end.atZone(ZoneId.of("UTC")).toInstant();

        return entityManager.createQuery("""
            SELECT r.id, r.user.id, r.user.nickname, r.content, r.rating,
                   r.book.id, r.book.title, r.book.thumbnailUrl,
                   COALESCE(r.likeCount, 0), COALESCE(r.commentCount, 0)
            FROM Review r
            WHERE r.createdAt >= :start AND r.createdAt < :end
              AND r.isDeleted = false
            ORDER BY COALESCE(r.likeCount, 0) DESC, COALESCE(r.commentCount, 0) DESC
        """, Object[].class)
            .setParameter("start", startInstant)
            .setParameter("end", endInstant)
            .setMaxResults(1000)
            .getResultList()
            .stream()
            .map(row -> new ReviewScoreDto(
                (UUID) row[0], (UUID) row[1], (String) row[2], (String) row[3],
                ((Number) row[4]).doubleValue(),
                (UUID) row[5], (String) row[6], (String) row[7],
                ((Number) row[8]).longValue(), ((Number) row[9]).longValue(),
                period
            ))
            .toList();
    }
}