package com.twogether.deokhugam.dashboard.batch;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.when;

import com.twogether.deokhugam.book.entity.Book;
import com.twogether.deokhugam.dashboard.dto.BookScoreDto;
import com.twogether.deokhugam.dashboard.entity.PopularBookRanking;
import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import com.twogether.deokhugam.dashboard.repository.PopularBookRankingRepository;
import com.twogether.deokhugam.review.repository.ReviewRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class PopularBookBatchServiceTest {

    private PopularBookBatchService batchService;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private PopularBookRankingRepository rankingRepository;

    @Mock
    private EntityManager em;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        batchService = new PopularBookBatchService(rankingRepository, reviewRepository);
        setField(batchService, "em", em);
    }

    @Test
    @DisplayName("Mockito 기반 배치 실행 테스트")
    void testCalculateAndSaveRanking_createsRankingCorrectly() {
        // given
        UUID bookId = UUID.randomUUID();
        BookScoreDto mockDto = new BookScoreDto(bookId, "제목", "저자", "http://example.com/thumb.jpg", 20, 4.8);
        LocalDateTime start = LocalDateTime.now().minusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = start.plusDays(1);

        when(reviewRepository.calculateBookScores(start, end)).thenReturn(List.of(mockDto));
        Book mockBook = new Book();
        when(em.getReference(Book.class, bookId)).thenReturn(mockBook);

        ArgumentCaptor<List<PopularBookRanking>> captor = ArgumentCaptor.forClass(List.class);
        when(rankingRepository.saveAll(captor.capture())).thenReturn(null);

        // when
        batchService.calculateAndSaveRanking(RankingPeriod.DAILY, start, end);

        // then
        List<PopularBookRanking> savedRankings = captor.getValue();
        assertThat(savedRankings).hasSize(1);
        PopularBookRanking ranking = savedRankings.get(0);
        assertThat(ranking.getScore()).isEqualTo(mockDto.calculateScore());
        assertThat(ranking.getTitle()).isEqualTo(mockDto.title());
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            var field = PopularBookBatchService.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}