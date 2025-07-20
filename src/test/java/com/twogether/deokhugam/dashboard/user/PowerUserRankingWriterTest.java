package com.twogether.deokhugam.dashboard.user;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.twogether.deokhugam.common.exception.DeokhugamException;
import com.twogether.deokhugam.common.exception.ErrorCode;
import com.twogether.deokhugam.dashboard.batch.writer.PowerUserRankingWriter;
import com.twogether.deokhugam.dashboard.entity.PowerUserRanking;
import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import com.twogether.deokhugam.dashboard.repository.PowerUserRankingRepository;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.Chunk;

@DisplayName("PowerUserRankingWriter 단위 테스트")
class PowerUserRankingWriterTest {

    private PowerUserRankingRepository repository;
    private PowerUserRankingWriter writer;

    @BeforeEach
    void setUp() {
        repository = mock(PowerUserRankingRepository.class);
        writer = new PowerUserRankingWriter(repository);
    }

    @Test
    @DisplayName("여러 개의 파워 유저 랭킹에 순위를 올바르게 부여하고 저장한다")
    void write_multipleRankings_assignSequentialRanks() {
        PowerUserRanking r1 = createRanking("user1", 12.0);
        PowerUserRanking r2 = createRanking("user2", 8.0);
        PowerUserRanking r3 = createRanking("user3", 4.0);
        Chunk<PowerUserRanking> chunk = new Chunk<>(List.of(r1, r2, r3));

        writer.write(chunk);

        assertEquals(1, r1.getRank());
        assertEquals(2, r2.getRank());
        assertEquals(3, r3.getRank());
        verify(repository).saveAll(List.of(r1, r2, r3));
    }

    @Test
    @DisplayName("동점자가 있을 때 동일한 순위를 부여한다")
    void write_sameScores_assignsSameRank() {
        PowerUserRanking r1 = createRanking("user1", 10.0);
        PowerUserRanking r2 = createRanking("user2", 10.0);
        PowerUserRanking r3 = createRanking("user3", 8.0);
        Chunk<PowerUserRanking> chunk = new Chunk<>(List.of(r1, r2, r3));

        writer.write(chunk);

        assertEquals(1, r1.getRank());
        assertEquals(1, r2.getRank());
        assertEquals(3, r3.getRank()); // 동점자 다음 순위는 건너뛰어야 함
        verify(repository).saveAll(List.of(r1, r2, r3));
    }

    @Test
    @DisplayName("빈 리스트가 들어오면 예외 발생")
    void write_emptyList_throwsException() {
        Chunk<PowerUserRanking> chunk = new Chunk<>(Collections.emptyList());

        assertThatThrownBy(() -> writer.write(chunk))
            .isInstanceOf(DeokhugamException.class)
            .hasMessageContaining(ErrorCode.RANKING_DATA_EMPTY.getMessage());
    }

    @Test
    @DisplayName("저장 중 예외 발생 시 래핑된 예외 발생")
    void write_repositoryFails_throwsException() {
        PowerUserRanking ranking = createRanking("user1", 10.0);
        Chunk<PowerUserRanking> chunk = new Chunk<>(List.of(ranking));

        doThrow(new RuntimeException("DB 에러")).when(repository).saveAll(any());

        assertThatThrownBy(() -> writer.write(chunk))
            .isInstanceOf(DeokhugamException.class)
            .hasMessageContaining(ErrorCode.RANKING_SAVE_FAILED.getMessage());
    }

    private PowerUserRanking createRanking(String nickname, double score) {
        return PowerUserRanking.builder()
            .user(null) // user 객체는 점수 계산에 영향 없음
            .nickname(nickname)
            .reviewScoreSum(0.0)
            .likeCount(0L)
            .commentCount(0L)
            .period(RankingPeriod.DAILY)
            .score(score)
            .rank(0)
            .createdAt(LocalDateTime.now())
            .build();
    }
}