package com.twogether.deokhugam.dashboard.review;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.twogether.deokhugam.common.exception.DeokhugamException;
import com.twogether.deokhugam.common.exception.ErrorCode;
import com.twogether.deokhugam.dashboard.batch.writer.PopularReviewRankingWriter;
import com.twogether.deokhugam.dashboard.entity.PopularReviewRanking;
import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import com.twogether.deokhugam.dashboard.repository.PopularReviewRankingRepository;
import com.twogether.deokhugam.notification.service.NotificationService;
import com.twogether.deokhugam.review.repository.ReviewRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.Chunk;

@ExtendWith(MockitoExtension.class)
@DisplayName("PopularReviewRankingWriter 단위 테스트")
class PopularReviewRankingWriterTest {

    PopularReviewRankingRepository repository;
    PopularReviewRankingWriter writer;

    @BeforeEach
    void setUp() {
        repository = mock(PopularReviewRankingRepository.class);
        NotificationService notificationService = mock(NotificationService.class);
        ReviewRepository reviewRepository = mock(ReviewRepository.class);

        writer = new PopularReviewRankingWriter(repository, notificationService, reviewRepository);
    }

    @Test
    @DisplayName("리뷰 랭킹 리스트에 순위를 부여하고 저장한다")
    void writer_shouldAssignRankAndSave() {
        // given
        PopularReviewRanking r1 = createRanking("책1", 9.5);
        PopularReviewRanking r2 = createRanking("책2", 7.8);
        Chunk<PopularReviewRanking> chunk = new Chunk<>(List.of(r1, r2));

        // when
        writer.write(chunk);

        // then
        assertEquals(1, r1.getRank());
        assertEquals(2, r2.getRank());
        verify(repository, times(1)).saveAll(List.of(r1, r2));
    }

    @Test
    @DisplayName("빈 랭킹 리스트인 경우 예외 발생")
    void writer_shouldThrowException_whenListIsEmpty() {
        Chunk<PopularReviewRanking> chunk = new Chunk<>(List.of());

        DeokhugamException ex = assertThrows(DeokhugamException.class, () -> writer.write(chunk));
        assertEquals(ErrorCode.RANKING_DATA_EMPTY, ex.getErrorCode());
    }

    @Test
    @DisplayName("저장 중 예외 발생 시 RANKING_SAVE_FAILED 예외를 던진다")
    void writer_shouldThrowException_whenRepositoryFails() {
        PopularReviewRanking r1 = createRanking("책", 8.0);
        Chunk<PopularReviewRanking> chunk = new Chunk<>(List.of(r1));

        doThrow(new RuntimeException("DB 에러"))
            .when(repository).saveAll(any());

        DeokhugamException ex = assertThrows(DeokhugamException.class, () -> writer.write(chunk));
        assertEquals(ErrorCode.RANKING_SAVE_FAILED, ex.getErrorCode());
    }

    private PopularReviewRanking createRanking(String title, double score) {
        return PopularReviewRanking.builder()
            .reviewId(UUID.randomUUID())
            .userId(UUID.randomUUID())
            .userNickname("테스터")
            .reviewContent("내용입니다")
            .reviewRating(4.5)
            .bookId(UUID.randomUUID())
            .bookTitle(title)
            .bookThumbnailUrl("http://example.com/thumb.jpg")
            .period(RankingPeriod.DAILY)
            .score(score)
            .likeCount(10L)
            .commentCount(20L)
            .rank(0)
            .build();
    }
}