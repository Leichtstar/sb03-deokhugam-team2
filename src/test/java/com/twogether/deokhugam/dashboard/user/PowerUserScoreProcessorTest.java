package com.twogether.deokhugam.dashboard.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.within;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.twogether.deokhugam.dashboard.batch.model.PowerUserScoreDto;
import com.twogether.deokhugam.dashboard.batch.processor.PowerUserScoreProcessor;
import com.twogether.deokhugam.dashboard.entity.PowerUserRanking;
import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import com.twogether.deokhugam.user.entity.User;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("PowerUserScoreProcessor 단위 테스트")
class PowerUserScoreProcessorTest {

    private EntityManager em;
    private Map<UUID, User> userMap;
    private PowerUserScoreProcessor processor;

    private final Instant executionTime = Instant.parse("2025-07-22T00:00:00Z");

    @BeforeEach
    void setUp() {
        em = mock(EntityManager.class);
    }

    @Test
    @DisplayName("유저가 존재하고 최근 활동이 1시간 이내면 bonus 0.003이 반영된다")
    void process_shouldApplyFreshnessBonus() {
        // given
        UUID userId = UUID.randomUUID();
        User user = mock(User.class);
        userMap = Map.of(userId, user);

        Instant recentActivityTime = Instant.now().minusSeconds(30 * 60); // 30분 전
        mockLatestActivityQueries(userId, recentActivityTime, null); // 리뷰만 있음

        PowerUserScoreDto dto = new PowerUserScoreDto(
            userId, "활동왕", 24.0, 3L, 5L, RankingPeriod.DAILY
        );

        processor = new PowerUserScoreProcessor(userMap, executionTime, new SimpleMeterRegistry(), em);

        double baseScore = dto.calculateScore();
        double expectedScore = baseScore + 0.003;

        // when
        PowerUserRanking result = processor.process(dto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.getScore()).isEqualTo(expectedScore, within(1e-3));
    }

    @Test
    @DisplayName("최신 활동이 없으면 bonus 없이 기본 점수만 반환된다")
    void process_shouldReturnBaseScore_whenNoActivity() {
        UUID userId = UUID.randomUUID();
        User user = mock(User.class);
        userMap = Map.of(userId, user);

        mockLatestActivityQueries(userId, null, null);

        PowerUserScoreDto dto = new PowerUserScoreDto(
            userId, "비활동유저", 16.0, 0L, 0L, RankingPeriod.DAILY
        );

        processor = new PowerUserScoreProcessor(userMap, executionTime, new SimpleMeterRegistry(), em);

        double expectedScore = dto.calculateScore();

        PowerUserRanking result = processor.process(dto);

        assertThat(result).isNotNull();
        assertThat(result.getScore()).isEqualTo(expectedScore, within(1e-3));
    }

    @Test
    @DisplayName("User가 존재하지 않으면 null을 반환한다")
    void process_userNotFound_returnsNull() {
        UUID userId = UUID.randomUUID();
        userMap = Map.of(); // 유저 없음

        PowerUserScoreDto dto = new PowerUserScoreDto(
            userId, "비회원", 20.0, 0L, 0L, RankingPeriod.DAILY
        );

        processor = new PowerUserScoreProcessor(userMap, executionTime, new SimpleMeterRegistry(), em);

        PowerUserRanking result = processor.process(dto);

        assertThat(result).isNull();
    }

    private void mockLatestActivityQueries(UUID userId, Instant reviewTime, Instant commentTime) {
        // mock review query
        TypedQuery<Instant> reviewQuery = mock(TypedQuery.class);
        when(em.createQuery(contains("FROM Review"), eq(Instant.class)))
            .thenReturn(reviewQuery);
        when(reviewQuery.setParameter("userId", userId)).thenReturn(reviewQuery);
        when(reviewQuery.getSingleResult()).thenReturn(reviewTime);

        // mock comment query
        TypedQuery<Instant> commentQuery = mock(TypedQuery.class);
        when(em.createQuery(contains("FROM Comment"), eq(Instant.class)))
            .thenReturn(commentQuery);
        when(commentQuery.setParameter("userId", userId)).thenReturn(commentQuery);
        when(commentQuery.getSingleResult()).thenReturn(commentTime);
    }
}