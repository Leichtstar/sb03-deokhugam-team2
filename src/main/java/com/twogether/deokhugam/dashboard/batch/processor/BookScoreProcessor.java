package com.twogether.deokhugam.dashboard.batch.processor;

import com.twogether.deokhugam.book.entity.Book;
import com.twogether.deokhugam.dashboard.batch.model.BookScoreDto;
import com.twogether.deokhugam.dashboard.entity.PopularBookRanking;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.persistence.EntityManager;
import java.time.Duration;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;

@Slf4j
public class BookScoreProcessor implements ItemProcessor<BookScoreDto, PopularBookRanking> {

    private final EntityManager em;
    private final MeterRegistry meterRegistry;

    public BookScoreProcessor(EntityManager em, MeterRegistry meterRegistry) {
        this.em = em;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public PopularBookRanking process(BookScoreDto dto) {
        // 처리 시간 측정을 위한 타이머 샘플 시작
        Timer.Sample sample = Timer.start(meterRegistry);

        Book book = em.find(Book.class, dto.bookId());
        if (book == null) {
            log.warn("[BookScoreProcessor] 도서 ID {} 에 해당하는 Book 엔티티가 존재하지 않아 점수 계산을 건너뜁니다.", dto.bookId());
            return null;
        }

        // 기존 점수 계산
        double score = dto.calculateScore();

        // 최신 리뷰 작성 시간 조회 (논리 삭제 제외: isDeleted = false)
        Instant latestReviewCreatedAt = em.createQuery(
                "SELECT MAX(r.createdAt) FROM Review r " +
                    "WHERE r.book.id = :bookId AND r.isDeleted = false", Instant.class)
            .setParameter("bookId", dto.bookId())
            .getSingleResult();

        // freshness bonus 계산
        double bonus = calculateFreshnessBonus(latestReviewCreatedAt);
        double finalScore = score + bonus;

        // 커스텀 메트릭 - 처리 건수 카운터 증가
        meterRegistry.counter("batch.popular_book.processed.count").increment();

        // 커스텀 메트릭 - 처리 시간 기록
        sample.stop(Timer.builder("batch.popular_book.processed.timer")
            .description("인기 도서 점수 계산에 소요된 시간")
            .tag("bookId", dto.bookId().toString())
            .register(meterRegistry));

        log.info("[BookScoreProcessor] 도서 '{}' (ID: {}) 점수 계산 완료 - 리뷰 수: {}, 평점 평균: {}, 기본 점수: {}, bonus: {}, 최종 점수: {}",
            dto.title(), dto.bookId(), dto.reviewCount(), dto.averageRating(), score, bonus, finalScore
        );

        return PopularBookRanking.builder()
            .book(book)
            .title(dto.title())
            .author(dto.author())
            .thumbnailUrl(dto.thumbnailUrl())
            .score(finalScore)
            .reviewCount(dto.reviewCount())
            .rating(dto.averageRating())
            .period(dto.period())
            .createdAt(Instant.now())
            .build();
    }

    private double calculateFreshnessBonus(Instant latestActivityTime) {
        if (latestActivityTime == null) return 0.0;

        long hoursAgo = Duration.between(latestActivityTime, Instant.now()).toHours();
        if (hoursAgo < 1) return 0.003;
        if (hoursAgo < 6) return 0.002;
        if (hoursAgo < 24) return 0.001;
        return 0.0;
    }
}