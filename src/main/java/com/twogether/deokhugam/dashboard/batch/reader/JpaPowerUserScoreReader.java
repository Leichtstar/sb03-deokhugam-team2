package com.twogether.deokhugam.dashboard.batch.reader;

import com.twogether.deokhugam.common.util.TimeParameterUtil;
import com.twogether.deokhugam.dashboard.batch.model.PowerUserScoreDto;
import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
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
        RankingPeriod period;
        try {
            period = RankingPeriod.valueOf(periodString);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("잘못된 랭킹 기간 파라미터입니다: " + periodString);
        }

        Instant now = TimeParameterUtil.parseNowOrDefault(nowString);
        boolean isAllTime = (period == RankingPeriod.ALL_TIME);
        Instant start = isAllTime ? null : period.getStartTime(now);
        Instant end = isAllTime ? null : period.getEndTime(now);

        // 1. 유저별 리뷰 활동 점수 집계 (likeCount * 0.3 + commentCount * 0.7)
        String reviewQuery = """
            SELECT r.user.id, SUM(COALESCE(r.likeCount, 0) * 0.3 + COALESCE(r.commentCount, 0) * 0.7)
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

        // 2. 유저별 리뷰 개수 (likeCount, commentCount를 합산하여 직접 계산)
        String userQuery = """
            SELECT r.user.id, SUM(COALESCE(r.likeCount, 0)), SUM(COALESCE(r.commentCount, 0))
            FROM Review r
            WHERE r.isDeleted = false
        """ + (isAllTime ? "" : " AND r.createdAt BETWEEN :start AND :end") + """
            GROUP BY r.user.id
        """;
        var userTypedQuery = em.createQuery(userQuery, Object[].class);
        if (!isAllTime) {
            userTypedQuery.setParameter("start", start);
            userTypedQuery.setParameter("end", end);
        }
        Map<UUID, Long> likeCountMap = new HashMap<>();
        Map<UUID, Long> commentCountMap = new HashMap<>();
        for (Object[] row : userTypedQuery.getResultList()) {
            UUID userId = (UUID) row[0];
            likeCountMap.put(userId, ((Number) row[1]).longValue());
            commentCountMap.put(userId, ((Number) row[2]).longValue());
        }

        // 유저 ID 수집
        Set<UUID> userIds = new HashSet<>(reviewScoreMap.keySet());
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

        // DTO 조립 및 정렬
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