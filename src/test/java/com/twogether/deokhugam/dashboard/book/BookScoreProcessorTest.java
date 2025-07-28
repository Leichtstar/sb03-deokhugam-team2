package com.twogether.deokhugam.dashboard.book;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.within;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.twogether.deokhugam.book.entity.Book;
import com.twogether.deokhugam.dashboard.batch.model.BookScoreDto;
import com.twogether.deokhugam.dashboard.batch.processor.BookScoreProcessor;
import com.twogether.deokhugam.dashboard.entity.PopularBookRanking;
import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("BookScoreProcessor 단위 테스트")
class BookScoreProcessorTest {

    private EntityManager em;
    private BookScoreProcessor processor;

    @BeforeEach
    void setUp() {
        em = mock(EntityManager.class);
        processor = new BookScoreProcessor(em, new SimpleMeterRegistry());
    }

    @Test
    @DisplayName("BookScoreDto를 받아 점수와 bonus를 계산하여 PopularBookRanking으로 반환한다")
    void process_validInput_returnsRankingWithBonus() {
        // given
        UUID bookId = UUID.randomUUID();
        Book book = mock(Book.class);
        when(em.find(Book.class, bookId)).thenReturn(book);

        Instant recentCreatedAt = Instant.now().minusSeconds(1800); // 30분 전
        mockLatestReviewCreatedAtQuery(bookId, recentCreatedAt);

        BookScoreDto dto = new BookScoreDto(
            bookId,
            "자바의 정석",
            "남궁성",
            "https://image.url",
            5L,
            4.0,
            RankingPeriod.DAILY
        );

        double expectedScore = dto.calculateScore();
        double expectedBonus = 0.003;
        double expectedFinalScore = expectedScore + expectedBonus;

        // when
        PopularBookRanking result = processor.process(dto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getBook()).isEqualTo(book);
        assertThat(result.getReviewCount()).isEqualTo(5L);
        assertThat(result.getRating()).isEqualTo(4.0);
        assertThat(result.getScore()).isEqualTo(expectedFinalScore, within(1e-6));
        assertThat(result.getPeriod()).isEqualTo(dto.period());
    }

    @Test
    @DisplayName("bookId에 해당하는 Book이 존재하지 않으면 null을 반환한다")
    void process_bookNotFound_returnsNull() {
        // given
        UUID bookId = UUID.randomUUID();
        when(em.find(Book.class, bookId)).thenReturn(null);

        BookScoreDto dto = new BookScoreDto(
            bookId,
            "Clean Code",
            "Robert C. Martin",
            "https://image.url",
            10L,
            4.5,
            RankingPeriod.DAILY
        );

        // when
        PopularBookRanking result = processor.process(dto);

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("가장 최신 리뷰가 24시간 이상인 경우 bonus는 0이다")
    void process_oldReview_noBonus() {
        // given
        UUID bookId = UUID.randomUUID();
        Book book = mock(Book.class);
        when(em.find(Book.class, bookId)).thenReturn(book);

        Instant oldCreatedAt = Instant.now().minusSeconds(60 * 60 * 25); // 25시간 전
        mockLatestReviewCreatedAtQuery(bookId, oldCreatedAt);

        BookScoreDto dto = new BookScoreDto(
            bookId,
            "Effective Java",
            "Joshua Bloch",
            "https://image.url",
            3L,
            4.2,
            RankingPeriod.DAILY
        );

        double expectedFinalScore = dto.calculateScore(); // bonus 없음

        // when
        PopularBookRanking result = processor.process(dto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getScore()).isEqualTo(expectedFinalScore, within(1e-6));
    }

    private void mockLatestReviewCreatedAtQuery(UUID bookId, Instant createdAt) {
        TypedQuery<Instant> query = mock(TypedQuery.class);
        when(em.createQuery(anyString(), eq(Instant.class))).thenReturn(query);
        when(query.setParameter(eq("bookId"), eq(bookId))).thenReturn(query);
        when(query.getSingleResult()).thenReturn(createdAt);
    }
}