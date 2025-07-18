package com.twogether.deokhugam.dashboard.batch.reader;

import com.twogether.deokhugam.dashboard.batch.model.BookScoreDto;
import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.batch.item.support.IteratorItemReader;

public class JpaBookScoreReader extends IteratorItemReader<BookScoreDto> {

    public JpaBookScoreReader(EntityManager em, RankingPeriod period) {
        super(fetchBookScores(em, period));
    }

    private static List<BookScoreDto> fetchBookScores(EntityManager em, RankingPeriod period) {
        String jpql = """
        SELECT new com.twogether.deokhugam.dashboard.batch.model.BookScoreDto(
            r.book.id,
            r.book.title,
            r.book.author,
            r.book.thumbnailUrl,
            COUNT(r),
            COALESCE(AVG(r.rating), 0)
        )
        FROM Review r
        WHERE r.createdAt >= :start AND r.createdAt < :end AND r.isDeleted = false
        GROUP BY r.book.id, r.book.title, r.book.author, r.book.thumbnailUrl
        ORDER BY COUNT(r) DESC, AVG(r.rating) DESC
        """;

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = period.getStartTime(now);
        LocalDateTime end = period.getEndTime(now);

        ZoneId zone = ZoneOffset.UTC;
        Instant startInstant = start.atZone(zone).toInstant();
        Instant endInstant = end.atZone(zone).toInstant();

        TypedQuery<BookScoreDto> query = em.createQuery(jpql, BookScoreDto.class);
        query.setParameter("start", startInstant);
        query.setParameter("end", endInstant);
        query.setMaxResults(1000);

        return query.getResultList();
    }
}