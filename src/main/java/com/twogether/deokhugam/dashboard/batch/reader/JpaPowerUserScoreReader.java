package com.twogether.deokhugam.dashboard.batch.reader;

import com.twogether.deokhugam.dashboard.batch.model.PowerUserScoreDto;
import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@StepScope
@Component
@RequiredArgsConstructor
public class JpaPowerUserScoreReader implements ItemReader<PowerUserScoreDto> {

    private final EntityManager em;

    @Value("#{jobParameters['period']}")
    private String periodString;

    @Value("#{jobParameters['now']}")
    private String nowString;

    private Iterator<PowerUserScoreDto> iterator;

    @Override
    public PowerUserScoreDto read() {
        if (iterator == null) {
            iterator = aggregatePowerUserScores().iterator();
        }
        return iterator.hasNext() ? iterator.next() : null;
    }

    private List<PowerUserScoreDto> aggregatePowerUserScores() {
        RankingPeriod period = RankingPeriod.valueOf(periodString);
        LocalDateTime now = LocalDateTime.parse(nowString);
        LocalDateTime start = period.getStartTime(now);
        LocalDateTime end = period.getEndTime(now);

        // 1. 작성한 리뷰의 인기 점수
        Map<UUID, Double> reviewScoreMap = em.createQuery("""
            SELECT r.user.id, SUM(r.likeCount * 0.3 + r.commentCount * 0.7)
            FROM Review r
            WHERE r.createdAt >= :start AND r.createdAt < :end
              AND r.isDeleted = false
            GROUP BY r.user.id
        """, Object[].class)
            .setParameter("start", start)
            .setParameter("end", end)
            .getResultList().stream()
            .collect(Collectors.toMap(
                row -> (UUID) row[0],
                row -> ((Number) row[1]).doubleValue()
            ));

        // 2. 좋아요 참여 수
        Map<UUID, Long> likeCountMap = em.createQuery("""
            SELECT l.reviewLikePK.userId, COUNT(l)
            FROM ReviewLike l
            WHERE l.liked = true
              AND l.review.createdAt >= :start AND l.review.createdAt < :end
            GROUP BY l.reviewLikePK.userId
        """, Object[].class)
            .setParameter("start", start)
            .setParameter("end", end)
            .getResultList().stream()
            .collect(Collectors.toMap(
                row -> (UUID) row[0],
                row -> ((Number) row[1]).longValue()
            ));

        // 3. 댓글 참여 수
        Map<UUID, Long> commentCountMap = em.createQuery("""
            SELECT c.user.id, COUNT(c)
            FROM Comment c
            WHERE c.createdAt >= :start AND c.createdAt < :end
              AND c.isDeleted = false
            GROUP BY c.user.id
        """, Object[].class)
            .setParameter("start", start)
            .setParameter("end", end)
            .getResultList().stream()
            .collect(Collectors.toMap(
                row -> (UUID) row[0],
                row -> ((Number) row[1]).longValue()
            ));

        // 통합 userId 목록
        Set<UUID> userIds = new HashSet<>();
        userIds.addAll(reviewScoreMap.keySet());
        userIds.addAll(likeCountMap.keySet());
        userIds.addAll(commentCountMap.keySet());

        // 닉네임 조회
        Map<UUID, String> nicknameMap = em.createQuery("""
            SELECT u.id, u.nickname
            FROM User u
            WHERE u.id IN :userIds
        """, Object[].class)
            .setParameter("userIds", userIds)
            .getResultList().stream()
            .collect(Collectors.toMap(
                row -> (UUID) row[0],
                row -> (String) row[1]
            ));

        // DTO 조립 후 정렬
        return userIds.stream()
            .map(userId -> new PowerUserScoreDto(
                userId,
                nicknameMap.getOrDefault(userId, "알 수 없음"),
                reviewScoreMap.getOrDefault(userId, 0.0),
                likeCountMap.getOrDefault(userId, 0L),
                commentCountMap.getOrDefault(userId, 0L),
                period
            ))
            .sorted(Comparator.comparingDouble(PowerUserScoreDto::calculateScore).reversed())
            .toList();
    }
}