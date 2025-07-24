package com.twogether.deokhugam.dashboard.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.twogether.deokhugam.dashboard.batch.model.PowerUserScoreDto;
import com.twogether.deokhugam.dashboard.batch.processor.PowerUserScoreProcessor;
import com.twogether.deokhugam.dashboard.entity.PowerUserRanking;
import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import com.twogether.deokhugam.user.entity.User;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("PowerUserScoreProcessor 단위 테스트")
class PowerUserScoreProcessorTest {

    private EntityManager em;
    private PowerUserScoreProcessor processor;

    @BeforeEach
    void setUp() {
        em = mock(EntityManager.class);
        processor = new PowerUserScoreProcessor(em, Instant.parse("2025-07-22T00:00:00Z"));
    }

    @Test
    @DisplayName("PowerUserScoreDto를 받아 PowerUserRanking으로 변환한다")
    void process_validInput_returnsRanking() {
        // given
        UUID userId = UUID.randomUUID();
        User user = mock(User.class);
        when(em.find(User.class, userId)).thenReturn(user);

        PowerUserScoreDto dto = new PowerUserScoreDto(
            userId,
            "활동왕",
            30.0,
            5L,
            10L,
            RankingPeriod.DAILY
        );

        // when
        PowerUserRanking result = processor.process(dto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.getNickname()).isEqualTo("활동왕");
        assertThat(result.getReviewScoreSum()).isEqualTo(30.0);
        assertThat(result.getLikeCount()).isEqualTo(5L);
        assertThat(result.getCommentCount()).isEqualTo(10L);

        double expectedScore = 30.0 * 0.5 + 5 * 0.2 + 10 * 0.3;
        assertThat(result.getScore()).isEqualTo(expectedScore);
        assertThat(result.getPeriod()).isEqualTo(dto.period());
        assertThat(result.getRank()).isEqualTo(0);
        assertThat(result.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("User가 존재하지 않으면 null을 반환한다")
    void process_userNotFound_returnsNull() {
        UUID userId = UUID.randomUUID();
        when(em.find(User.class, userId)).thenReturn(null);

        PowerUserScoreDto dto = new PowerUserScoreDto(
            userId,
            "비회원",
            20.0,
            0L,
            0L,
            RankingPeriod.DAILY
        );

        PowerUserRanking result = processor.process(dto);

        assertThat(result).isNull();
    }
}