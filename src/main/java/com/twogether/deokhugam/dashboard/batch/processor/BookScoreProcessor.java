package com.twogether.deokhugam.dashboard.batch.processor;

import com.twogether.deokhugam.book.entity.Book;
import com.twogether.deokhugam.dashboard.batch.model.BookScoreDto;
import com.twogether.deokhugam.dashboard.entity.PopularBookRanking;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import org.springframework.batch.item.ItemProcessor;

public class BookScoreProcessor implements ItemProcessor<BookScoreDto, PopularBookRanking> {

    private final EntityManager em;

    public BookScoreProcessor(EntityManager em) {
        this.em = em;
    }

    @Override
    public PopularBookRanking process(BookScoreDto dto) {
        Book book = em.find(Book.class, dto.bookId());
        if (book == null) return null;

        return PopularBookRanking.builder()
            .book(book)
            .title(dto.title())
            .author(dto.author())
            .thumbnailUrl(dto.thumbnailUrl())
            .score(dto.calculateScore())
            .reviewCount(dto.reviewCount())
            .rating(dto.averageRating())
            .period(dto.period())
            .createdAt(Instant.now())
            .build();
    }
}