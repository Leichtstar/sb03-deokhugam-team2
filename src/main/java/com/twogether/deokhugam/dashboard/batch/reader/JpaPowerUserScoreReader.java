package com.twogether.deokhugam.dashboard.batch.reader;

import com.twogether.deokhugam.common.util.TimeParameterUtil;
import com.twogether.deokhugam.dashboard.batch.model.PowerUserScoreDto;
import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.Collections;
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
        Instant now = TimeParameterUtil.parseNowOrDefault(nowString);
        boolean isAllTime = (period == RankingPeriod.ALL_TIME);
        Instant start = isAllTime ? null : period.getStartTime(now);
        Instant end = isAllTime ? null : period.getEndTime(now);

        // 1. 리뷰 점수
        String reviewQuery = """
        SELECT r.user.id, SUM(r.likeCount * 0.3 + r.commentCount * 0.7)
        FROM Review r
        WHERE r.isDeleted = false
    """ + (isAllTime ? "" : " AND r.createdAt BETWEEN :start AND :end") + """
        GROUP BY r.user.id
    """;

        var reviewTypedQuery = em.createQuery(reviewQuery, Object[].class);
        if (!isAllTime) {
            reviewTypedQuery.setParameter("start", start);
            reviewTypedQuery.setParameter("end", end);
        }
        Map<UUID, Double> reviewScoreMap = reviewTypedQuery.getResultList().stream()
            .collect(Collectors.toMap(
                row -> (UUID) row[0],
                row -> ((Number) row[1]).doubleValue()
            ));

        // 2. 좋아요 수
        String likeQuery = """
        SELECT l.reviewLikePK.userId, COUNT(l)
        FROM ReviewLike l
        WHERE l.liked = true
    """ + (isAllTime ? "" : " AND l.review.createdAt BETWEEN :start AND :end") + """
        GROUP BY l.reviewLikePK.userId
    """;

        var likeTypedQuery = em.createQuery(likeQuery, Object[].class);
        if (!isAllTime) {
            likeTypedQuery.setParameter("start", start);
            likeTypedQuery.setParameter("end", end);
        }
        Map<UUID, Long> likeCountMap = likeTypedQuery.getResultList().stream()
            .collect(Collectors.toMap(
                row -> (UUID) row[0],
                row -> ((Number) row[1]).longValue()
            ));

        // 3. 댓글 수
        String commentQuery = """
        SELECT c.user.id, COUNT(c)
        FROM Comment c
        WHERE c.isDeleted = false
    """ + (isAllTime ? "" : " AND c.createdAt BETWEEN :start AND :end") + """
        GROUP BY c.user.id
    """;

        var commentTypedQuery = em.createQuery(commentQuery, Object[].class);
        if (!isAllTime) {
            commentTypedQuery.setParameter("start", start);
            commentTypedQuery.setParameter("end", end);
        }
        Map<UUID, Long> commentCountMap = commentTypedQuery.getResultList().stream()
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
        Map<UUID, String> nicknameMap = userIds.isEmpty() ? Collections.emptyMap() :
            em.createQuery("""
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

        // DTO 조립 및 점수 기준 정렬
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