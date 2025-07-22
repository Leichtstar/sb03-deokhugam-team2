package com.twogether.deokhugam.dashboard.batch.reader;

import com.twogether.deokhugam.dashboard.batch.model.BookScoreDto;
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
public class JpaBookScoreReader implements ItemReader<BookScoreDto> {

    private final EntityManager entityManager;

    @Value("#{jobParameters['period']}")
    private String periodString;

    @Value("#{jobParameters['now']}")
    private String nowString;

    private Iterator<BookScoreDto> bookIterator;

    @Override
    public BookScoreDto read() {
        if (bookIterator == null) {
            bookIterator = fetchBookScores().iterator();
        }
        return bookIterator.hasNext() ? bookIterator.next() : null;
    }

    private List<BookScoreDto> fetchBookScores() {
        RankingPeriod period = RankingPeriod.valueOf(periodString);
        LocalDateTime now = LocalDateTime.parse(nowString);

        Instant start = period.getStartTime(now).atZone(ZoneId.of("UTC")).toInstant();
        Instant end = period.getEndTime(now).atZone(ZoneId.of("UTC")).toInstant();

        return entityManager.createQuery("""
            SELECT b.id, b.title, b.author, b.thumbnailUrl,
                   COUNT(r), COALESCE(AVG(r.rating), 0.0)
            FROM Review r
            JOIN r.book b
            WHERE r.createdAt >= :start AND r.createdAt < :end
              AND r.isDeleted = false AND b.isDeleted = false
            GROUP BY b.id, b.title, b.author, b.thumbnailUrl
            ORDER BY COUNT(r) DESC, AVG(r.rating) DESC
        """, Object[].class)
            .setParameter("start", start)
            .setParameter("end", end)
            .setMaxResults(1000)
            .getResultList()
            .stream()
            .map(row -> new BookScoreDto(
                (UUID) row[0],
                (String) row[1],
                (String) row[2],
                (String) row[3],
                (Long) row[4],
                ((Number) row[5]).doubleValue(),
                period
            ))
            .toList();
    }
}