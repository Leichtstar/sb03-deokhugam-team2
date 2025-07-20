package com.twogether.deokhugam.dashboard.batch.reader;

import com.twogether.deokhugam.dashboard.batch.model.PowerUserScoreDto;
import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.springframework.batch.item.support.IteratorItemReader;

public class JpaPowerUserScoreReader extends IteratorItemReader<PowerUserScoreDto> {

    public JpaPowerUserScoreReader(EntityManager em, RankingPeriod period) {
        super(fetchPowerUserScores(em, period));
    }

    private static List<PowerUserScoreDto> fetchPowerUserScores(EntityManager em, RankingPeriod period) {
        String jpql = """
            SELECT new com.twogether.deokhugam.dashboard.batch.model.PowerUserScoreDto(
                r.user.id,
                r.userNickName,
                SUM(
                    COALESCE(r.likeCount, 0) * 0.3 + COALESCE(r.commentCount, 0) * 0.7
                ),
                SUM(COALESCE(r.likeCount, 0)),
                SUM(COALESCE(r.commentCount, 0))
            )
            FROM Review r
            WHERE r.createdAt >= :start
              AND r.createdAt < :end
              AND r.isDeleted = false
            GROUP BY r.user.id, r.userNickName
            ORDER BY SUM(
                COALESCE(r.likeCount, 0) * 0.3 + COALESCE(r.commentCount, 0) * 0.7
            ) DESC
        """;

        Instant now = Instant.now();
        LocalDateTime utcNow = LocalDateTime.ofInstant(now, ZoneId.of("UTC"));
        LocalDateTime start = period.getStartTime(utcNow);
        LocalDateTime end = period.getEndTime(utcNow);

        Instant startInstant = start.atZone(ZoneId.of("UTC")).toInstant();
        Instant endInstant = end.atZone(ZoneId.of("UTC")).toInstant();

        TypedQuery<PowerUserScoreDto> query = em.createQuery(jpql, PowerUserScoreDto.class);
        query.setParameter("start", startInstant);
        query.setParameter("end", endInstant);
        query.setMaxResults(1000);

        return query.getResultList();
    }
}