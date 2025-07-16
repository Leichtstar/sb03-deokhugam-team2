package com.twogether.deokhugam.dashboard.batch;

import com.twogether.deokhugam.book.entity.Book;
import com.twogether.deokhugam.dashboard.batch.model.BookScoreDto;
import com.twogether.deokhugam.dashboard.entity.PopularBookRanking;
import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import org.springframework.batch.item.ItemProcessor;

public class BookScoreProcessor implements ItemProcessor<BookScoreDto, PopularBookRanking> {

    private final EntityManager em;
    private final String periodKey;

    public BookScoreProcessor(EntityManager em, String periodKey) {
        this.em = em;
        this.periodKey = periodKey;
    }

    @Override
    public PopularBookRanking process(BookScoreDto dto) {
        RankingPeriod period = RankingPeriod.valueOf(periodKey.toUpperCase());

        Book book = em.find(Book.class, dto.bookId());
        if (book == null) return null;

        return PopularBookRanking.builder()
            .book(book)
            .title(dto.title())
            .author(dto.author())
            .thumbnailUrl(dto.thumbnailUrl())
            .score(dto.calculateScore())
            .period(period)
            .createdAt(LocalDateTime.now())
            .build();
    }
}