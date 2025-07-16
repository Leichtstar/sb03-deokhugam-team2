package com.twogether.deokhugam.dashboard.batch;

import com.twogether.deokhugam.dashboard.batch.model.BookScoreDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.batch.item.support.IteratorItemReader;

public class JpaBookScoreReader extends IteratorItemReader<BookScoreDto> {

    public JpaBookScoreReader(EntityManager em, LocalDateTime start, LocalDateTime end) {
        super(fetchBookScores(em, start, end));
    }

    private static List<BookScoreDto> fetchBookScores(EntityManager em, LocalDateTime start, LocalDateTime end) {
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

        TypedQuery<BookScoreDto> query = em.createQuery(jpql, BookScoreDto.class);
        query.setParameter("start", start);
        query.setParameter("end", end);

        return query.getResultList();
    }
}