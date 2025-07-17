package com.twogether.deokhugam.dashboard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.twogether.deokhugam.book.entity.Book;
import com.twogether.deokhugam.dashboard.batch.processor.BookScoreProcessor;
import com.twogether.deokhugam.dashboard.batch.model.BookScoreDto;
import com.twogether.deokhugam.dashboard.entity.PopularBookRanking;
import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import jakarta.persistence.EntityManager;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BookScoreProcessorTest {

    private EntityManager em;
    private BookScoreProcessor processor;

    @BeforeEach
    void setUp() {
        em = mock(EntityManager.class);
        processor = new BookScoreProcessor(em, RankingPeriod.DAILY);
    }

    @Test
    @DisplayName("BookScoreDto를 받아 점수를 계산하여 PopularBookRanking으로 반환한다")
    void process_validInput_returnsRanking() {
        // given
        UUID bookId = UUID.randomUUID();
        Book book = mock(Book.class);
        when(em.find(Book.class, bookId)).thenReturn(book);

        BookScoreDto dto = new BookScoreDto(
            bookId,
            "자바의 정석",
            "남궁성",
            "https://image.url",
            5L,
            4.0
        );

        // when
        PopularBookRanking result = processor.process(dto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getBook()).isEqualTo(book);
        assertThat(result.getReviewCount()).isEqualTo(5L);
        assertThat(result.getRating()).isEqualTo(4.0);
        assertThat(result.getScore()).isEqualTo(dto.calculateScore());
        assertThat(result.getPeriod()).isEqualTo(RankingPeriod.DAILY);
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
            4.5
        );

        // when
        PopularBookRanking result = processor.process(dto);

        // then
        assertThat(result).isNull();
    }
}