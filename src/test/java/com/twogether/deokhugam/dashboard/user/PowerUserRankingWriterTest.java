package com.twogether.deokhugam.dashboard.user;

import static org.assertj.core.api.Assertions.assertThatCode;
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
import java.time.Instant;
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
    @DisplayName("동점자가 있는 경우 같은 순위를 부여하고 다음 순위를 올바르게 계산한다")
    void write_tiedScores_assignCorrectRanks() {
        PowerUserRanking r1 = createRanking("user1", 10.0);
        PowerUserRanking r2 = createRanking("user2", 10.0); // 동점
        PowerUserRanking r3 = createRanking("user3", 8.0);
        PowerUserRanking r4 = createRanking("user4", 8.0);  // 동점
        PowerUserRanking r5 = createRanking("user5", 6.0);
        Chunk<PowerUserRanking> chunk = new Chunk<>(List.of(r1, r2, r3, r4, r5));

        writer.write(chunk);

        assertEquals(1, r1.getRank()); // 1위
        assertEquals(1, r2.getRank()); // 1위 (동점)
        assertEquals(3, r3.getRank()); // 3위 (2위 건너뜀)
        assertEquals(3, r4.getRank()); // 3위 (동점)
        assertEquals(5, r5.getRank()); // 5위 (4위 건너뜀)
    }

    @Test
    @DisplayName("빈 리스트가 들어오면 스킵 처리됨")
    void write_emptyList_skipsWithoutException() {
        // given
        Chunk<PowerUserRanking> chunk = new Chunk<>(Collections.emptyList());

        // when & then: 예외 없이 정상 종료되는지 확인
        assertThatCode(() -> writer.write(chunk)).doesNotThrowAnyException();

        // (선택) 로그 검증 예시 - logback 테스트 도구를 사용할 경우
        // verify(log).warn("파워 유저 랭킹 저장 스킵: 저장할 데이터가 없습니다.");
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
            .createdAt(Instant.now())
            .build();
    }
}