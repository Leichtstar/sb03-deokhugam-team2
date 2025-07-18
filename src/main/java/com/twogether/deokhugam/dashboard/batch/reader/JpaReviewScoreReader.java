package com.twogether.deokhugam.dashboard.batch.reader;

import com.twogether.deokhugam.dashboard.batch.model.ReviewScoreDto;
import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.springframework.batch.item.support.IteratorItemReader;

public class JpaReviewScoreReader extends IteratorItemReader<ReviewScoreDto> {

    public JpaReviewScoreReader(EntityManager em, RankingPeriod period) {
        super(fetchReviewScores(em, period));
    }

    private static List<ReviewScoreDto> fetchReviewScores(EntityManager em, RankingPeriod period) {
        String jpql = """
            SELECT new com.twogether.deokhugam.dashboard.batch.model.ReviewScoreDto(
                r.id,
                r.user.id,
                r.userNickName,
                r.content,
                r.rating,
                r.book.id,
                r.bookTitle,
                r.bookThumbnailUrl,
                COALESCE(r.likeCount, 0),
                COALESCE(r.commentCount, 0)
            )
            FROM Review r
            WHERE r.createdAt >= :start
              AND r.createdAt < :end
              AND r.isDeleted = false
            ORDER BY COALESCE(r.likeCount, 0) DESC, COALESCE(r.commentCount, 0) DESC
        """;

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = period.getStartTime(now);
        LocalDateTime end = period.getEndTime(now);

        Instant startInstant = start.atZone(ZoneId.of("UTC")).toInstant();
        Instant endInstant = end.atZone(ZoneId.of("UTC")).toInstant();

        TypedQuery<ReviewScoreDto> query = em.createQuery(jpql, ReviewScoreDto.class);
        query.setParameter("start", startInstant);
        query.setParameter("end", endInstant);
        query.setMaxResults(1000);

        return query.getResultList();
    }
}