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
            SELECT r.book.id, r.book.title, r.book.author, r.book.thumbnailUrl, COUNT(r), COALESCE(AVG(r.rating), 0)
            FROM Review r
            WHERE r.createdAt >= :start AND r.createdAt < :end
              AND r.isDeleted = false AND r.book.isDeleted = false
            GROUP BY r.book.id, r.book.title, r.book.author, r.book.thumbnailUrl
            ORDER BY COUNT(r) DESC, AVG(r.rating) DESC
        """, Object[].class)
            .setParameter("start", startInstant)
            .setParameter("end", endInstant)
            .setMaxResults(1000)
            .getResultList()
            .stream()
            .map(row -> new BookScoreDto(
                (UUID) row[0],
                (String) row[1],
                (String) row[2],
                (String) row[3],
                (Long) row[4],
                (Double) row[5],
                period
            ))
            .toList();
    }
}