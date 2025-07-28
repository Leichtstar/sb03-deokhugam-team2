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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
        PowerUserRanking r1 = createRanking("user1", 12.0, Instant.parse("2025-07-25T10:00:00Z"));
        PowerUserRanking r2 = createRanking("user2", 8.0, Instant.parse("2025-07-24T10:00:00Z"));
        PowerUserRanking r3 = createRanking("user3", 4.0, Instant.parse("2025-07-23T10:00:00Z"));
        Chunk<PowerUserRanking> chunk = new Chunk<>(List.of(r1, r2, r3));

        writer.write(chunk);

        List<PowerUserRanking> result = new ArrayList<>(List.of(r1, r2, r3));
        result.sort(Comparator.comparingInt(PowerUserRanking::getRank));

        assertEquals("user1", result.get(0).getNickname());
        assertEquals(1, result.get(0).getRank());

        assertEquals("user2", result.get(1).getNickname());
        assertEquals(2, result.get(1).getRank());

        assertEquals("user3", result.get(2).getNickname());
        assertEquals(3, result.get(2).getRank());

        verify(repository).saveAll(any());
    }

    @Test
    @DisplayName("동점자가 있는 경우 score DESC, createdAt ASC 기준으로 순위를 부여한다")
    void write_tiedScores_assignCorrectRanks() {
        // given
        PowerUserRanking r1 = createRanking("user1", 10.0, Instant.parse("2025-07-25T10:00:00Z")); // 늦게 생성
        PowerUserRanking r2 = createRanking("user2", 10.0, Instant.parse("2025-07-24T10:00:00Z")); // 먼저 생성
        PowerUserRanking r3 = createRanking("user3", 8.0, Instant.parse("2025-07-23T10:00:00Z"));
        PowerUserRanking r4 = createRanking("user4", 8.0, Instant.parse("2025-07-22T10:00:00Z"));
        PowerUserRanking r5 = createRanking("user5", 6.0, Instant.parse("2025-07-21T10:00:00Z"));
        Chunk<PowerUserRanking> chunk = new Chunk<>(List.of(r1, r2, r3, r4, r5)); // 순서 의도적으로 뒤섞음

        // when
        writer.write(chunk);

        // then
        List<PowerUserRanking> rank1List = List.of(r1, r2).stream()
            .filter(r -> r.getRank() == 1)
            .sorted(Comparator.comparing(PowerUserRanking::getCreatedAt))
            .toList();

        assertEquals(1, r1.getRank());
        assertEquals(1, r2.getRank());
        assertEquals("user2", rank1List.get(0).getNickname()); // 먼저 생성된 user2
        assertEquals("user1", rank1List.get(1).getNickname());

        List<PowerUserRanking> rank3List = List.of(r3, r4).stream()
            .filter(r -> r.getRank() == 3)
            .sorted(Comparator.comparing(PowerUserRanking::getCreatedAt))
            .toList();

        assertEquals(3, r3.getRank());
        assertEquals(3, r4.getRank());
        assertEquals("user4", rank3List.get(0).getNickname());
        assertEquals("user3", rank3List.get(1).getNickname());

        assertEquals(5, r5.getRank());
    }

    @Test
    @DisplayName("빈 리스트가 들어오면 스킵 처리됨")
    void write_emptyList_skipsWithoutException() {
        Chunk<PowerUserRanking> chunk = new Chunk<>(Collections.emptyList());
        assertThatCode(() -> writer.write(chunk)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("저장 중 예외 발생 시 래핑된 예외 발생")
    void write_repositoryFails_throwsException() {
        PowerUserRanking ranking = createRanking("user1", 10.0, Instant.now());
        Chunk<PowerUserRanking> chunk = new Chunk<>(List.of(ranking));

        doThrow(new RuntimeException("DB 에러")).when(repository).saveAll(any());

        assertThatThrownBy(() -> writer.write(chunk))
            .isInstanceOf(DeokhugamException.class)
            .hasMessageContaining(ErrorCode.RANKING_SAVE_FAILED.getMessage());
    }

    private PowerUserRanking createRanking(String nickname, double score, Instant createdAt) {
        return PowerUserRanking.builder()
            .user(null)
            .nickname(nickname)
            .reviewScoreSum(0.0)
            .likeCount(0L)
            .commentCount(0L)
            .period(RankingPeriod.DAILY)
            .score(score)
            .rank(0)
            .createdAt(createdAt)
            .build();
    }
}