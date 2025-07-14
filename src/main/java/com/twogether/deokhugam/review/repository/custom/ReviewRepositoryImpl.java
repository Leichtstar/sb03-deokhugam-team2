package com.twogether.deokhugam.review.repository.custom;

import com.twogether.deokhugam.dashboard.dto.BookScoreDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class ReviewRepositoryImpl implements ReviewRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<BookScoreDto> calculateBookScores(LocalDateTime start, LocalDateTime end) {
        return em.createQuery("""
            SELECT new com.twogether.deokhugam.dashboard.dto.BookScoreDto(
            r.book.id,
            r.book.title,
            r.book.author,
            r.book.thumbnailUrl,
            COUNT(r),
            COALESCE(AVG(r.rating), 0) 
          )
          FROM Review r
          WHERE r.createdAt >= :start AND r.createdAt < :end
          GROUP BY r.book.id, r.book.title, r.book.author, r.book.thumbnailUrl
          ORDER BY COUNT(r) DESC, AVG(r.rating) DESC
        """, BookScoreDto.class)
        .setParameter("start", start)
        .setParameter("end", end)
        .getResultList();
    }

    @Override
    public List<BookScoreDto> calculateBookScoresAllTime() {
        String jpql = """
        SELECT new com.twogether.deokhugam.dashboard.dto.BookScoreDto(
            r.book.id,
            r.book.title,
            r.book.author,
            r.book.thumbnailUrl,
            COUNT(r),
            AVG(r.rating)
        )
        FROM Review r
        GROUP BY r.book.id, r.book.title, r.book.author, r.book.thumbnailUrl
        ORDER BY COUNT(r) DESC, AVG(r.rating) DESC
    """;

        return em.createQuery(jpql, BookScoreDto.class).getResultList();
    }
}
