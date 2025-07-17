package com.twogether.deokhugam.dashboard.book;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.twogether.deokhugam.common.exception.DeokhugamException;
import com.twogether.deokhugam.common.exception.ErrorCode;
import com.twogether.deokhugam.dashboard.batch.writer.PopularBookRankingWriter;
import com.twogether.deokhugam.dashboard.entity.PopularBookRanking;
import com.twogether.deokhugam.dashboard.repository.PopularBookRankingRepository;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.Chunk;

class PopularBookRankingWriterTest {

    private PopularBookRankingRepository repository;
    private PopularBookRankingWriter writer;

    @BeforeEach
    void setUp() {
        repository = mock(PopularBookRankingRepository.class);
        writer = new PopularBookRankingWriter(repository);
    }

    @Test
    @DisplayName("정상적으로 랭킹 데이터를 저장하고 assignRank를 호출한다")
    void write_successful() {
        // given
        PopularBookRanking ranking = mock(PopularBookRanking.class);
        Chunk<PopularBookRanking> chunk = new Chunk<>(List.of(ranking));

        // when
        writer.write(chunk);

        // then
        verify(ranking, times(1)).assignRank(1); // assignRank(1) 호출 확인
        verify(repository, times(1)).saveAll(List.of(ranking));
    }

    @Test
    @DisplayName("빈 리스트가 전달되면 RANKING_DATA_EMPTY 예외를 던진다")
    void write_emptyList_throwsException() {
        // given
        Chunk<PopularBookRanking> emptyChunk = new Chunk<>(Collections.emptyList());

        // expect
        assertThatThrownBy(() -> writer.write(emptyChunk))
            .isInstanceOf(DeokhugamException.class)
            .hasMessageContaining(ErrorCode.RANKING_DATA_EMPTY.getMessage());
    }

    @Test
    @DisplayName("저장 중 예외가 발생하면 RANKING_SAVE_FAILED 예외를 던진다")
    void write_repositoryThrows_throwsWrappedException() {
        // given
        PopularBookRanking ranking = mock(PopularBookRanking.class);
        Chunk<PopularBookRanking> chunk = new Chunk<>(List.of(ranking));
        doThrow(new RuntimeException("DB 에러")).when(repository).saveAll(any());

        // expect
        assertThatThrownBy(() -> writer.write(chunk))
            .isInstanceOf(DeokhugamException.class)
            .hasMessageContaining(ErrorCode.RANKING_SAVE_FAILED.getMessage());
    }

    @Test
    @DisplayName("여러 개의 랭킹에 assignRank가 순차적으로 호출된다")
    void write_multipleRankings_assignsSequentialRank() {
        // given
        PopularBookRanking r1 = mock(PopularBookRanking.class);
        PopularBookRanking r2 = mock(PopularBookRanking.class);
        PopularBookRanking r3 = mock(PopularBookRanking.class);
        Chunk<PopularBookRanking> chunk = new Chunk<>(List.of(r1, r2, r3));

        // when
        writer.write(chunk);

        // then
        verify(r1).assignRank(1);
        verify(r2).assignRank(2);
        verify(r3).assignRank(3);
        verify(repository).saveAll(List.of(r1, r2, r3));
    }
}
