package com.twogether.deokhugam.dashboard.book;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.twogether.deokhugam.common.exception.DeokhugamException;
import com.twogether.deokhugam.common.exception.ErrorCode;
import com.twogether.deokhugam.dashboard.batch.writer.PopularBookRankingWriter;
import com.twogether.deokhugam.dashboard.entity.PopularBookRanking;
import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import com.twogether.deokhugam.dashboard.repository.PopularBookRankingRepository;
import java.time.Instant;
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
        given(ranking.getScore()).willReturn(5.0);
        given(ranking.getCreatedAt()).willReturn(Instant.parse("2025-07-25T10:00:00Z"));
        given(ranking.getPeriod()).willReturn(RankingPeriod.DAILY);

        Chunk<PopularBookRanking> chunk = new Chunk<>(List.of(ranking));

        // when
        writer.write(chunk);

        // then
        verify(ranking).assignRank(1);
        verify(repository).saveAll(List.of(ranking));
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
        given(ranking.getScore()).willReturn(4.0);
        given(ranking.getCreatedAt()).willReturn(Instant.parse("2025-07-25T10:00:00Z"));
        given(ranking.getPeriod()).willReturn(RankingPeriod.DAILY);

        Chunk<PopularBookRanking> chunk = new Chunk<>(List.of(ranking));
        doThrow(new RuntimeException("DB 에러")).when(repository).saveAll(any());

        // expect
        assertThatThrownBy(() -> writer.write(chunk))
            .isInstanceOf(DeokhugamException.class)
            .hasMessageContaining(ErrorCode.RANKING_SAVE_FAILED.getMessage());
    }

    @Test
    @DisplayName("여러 개의 랭킹에 assignRank가 동점 + 등록시간 오름차순 기준으로 호출된다")
    void write_multipleRankings_assignsRank_withTieBreaker() {
        // given
        PopularBookRanking r1 = mock(PopularBookRanking.class);
        PopularBookRanking r2 = mock(PopularBookRanking.class);
        PopularBookRanking r3 = mock(PopularBookRanking.class);

        // r1, r2: 동점 (5.0), r2가 더 먼저 생성됨
        given(r1.getScore()).willReturn(5.0);
        given(r2.getScore()).willReturn(5.0);
        given(r3.getScore()).willReturn(4.0);

        given(r1.getCreatedAt()).willReturn(Instant.parse("2025-07-25T10:00:00Z"));
        given(r2.getCreatedAt()).willReturn(Instant.parse("2025-07-24T10:00:00Z")); // 더 먼저
        given(r3.getCreatedAt()).willReturn(Instant.parse("2025-07-23T10:00:00Z"));

        given(r1.getPeriod()).willReturn(RankingPeriod.DAILY);

        Chunk<PopularBookRanking> chunk = new Chunk<>(List.of(r1, r2, r3));

        // when
        writer.write(chunk);

        // then: 정렬 순서 → r2 (rank 1), r1 (rank 1), r3 (rank 3)
        verify(r2).assignRank(1);
        verify(r1).assignRank(1);
        verify(r3).assignRank(3);

        verify(repository).saveAll(List.of(r2, r1, r3));
    }
}
