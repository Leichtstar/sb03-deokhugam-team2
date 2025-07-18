package com.twogether.deokhugam.dashboard.user;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.twogether.deokhugam.common.exception.DeokhugamException;
import com.twogether.deokhugam.common.exception.ErrorCode;
import com.twogether.deokhugam.dashboard.batch.writer.PowerUserRankingWriter;
import com.twogether.deokhugam.dashboard.entity.PowerUserRanking;
import com.twogether.deokhugam.dashboard.repository.PowerUserRankingRepository;
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
    @DisplayName("단일 랭킹 저장 시 assignRank(1) 호출 및 저장 확인")
    void write_singleRanking_success() {
        PowerUserRanking ranking = mock(PowerUserRanking.class);
        Chunk<PowerUserRanking> chunk = new Chunk<>(List.of(ranking));

        writer.write(chunk);

        verify(ranking).assignRank(1);
        verify(repository).saveAll(List.of(ranking));
    }

    @Test
    @DisplayName("빈 리스트가 들어오면 RANKING_DATA_EMPTY 예외 발생")
    void write_emptyList_throwsRankingDataEmpty() {
        Chunk<PowerUserRanking> chunk = new Chunk<>(Collections.emptyList());

        assertThatThrownBy(() -> writer.write(chunk))
            .isInstanceOf(DeokhugamException.class)
            .hasMessageContaining(ErrorCode.RANKING_DATA_EMPTY.getMessage());
    }

    @Test
    @DisplayName("여러 랭킹에 순차적으로 assignRank가 호출됨")
    void write_multipleRankings_assignRanks() {
        PowerUserRanking r1 = mock(PowerUserRanking.class);
        PowerUserRanking r2 = mock(PowerUserRanking.class);
        PowerUserRanking r3 = mock(PowerUserRanking.class);
        Chunk<PowerUserRanking> chunk = new Chunk<>(List.of(r1, r2, r3));

        writer.write(chunk);

        verify(r1).assignRank(1);
        verify(r2).assignRank(2);
        verify(r3).assignRank(3);
        verify(repository).saveAll(List.of(r1, r2, r3));
    }

    @Test
    @DisplayName("저장 중 예외 발생 시 RANKING_SAVE_FAILED 예외 발생")
    void write_repositoryFails_throwsRankingSaveFailed() {
        PowerUserRanking ranking = mock(PowerUserRanking.class);
        Chunk<PowerUserRanking> chunk = new Chunk<>(List.of(ranking));

        doThrow(new RuntimeException("DB 오류")).when(repository).saveAll(any());

        assertThatThrownBy(() -> writer.write(chunk))
            .isInstanceOf(DeokhugamException.class)
            .hasMessageContaining(ErrorCode.RANKING_SAVE_FAILED.getMessage());
    }
}