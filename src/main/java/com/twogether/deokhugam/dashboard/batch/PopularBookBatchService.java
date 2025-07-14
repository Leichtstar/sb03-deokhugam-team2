package com.twogether.deokhugam.dashboard.batch;

import com.twogether.deokhugam.book.entity.Book;
import com.twogether.deokhugam.dashboard.dto.BookScoreDto;
import com.twogether.deokhugam.dashboard.entity.PopularBookRanking;
import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import com.twogether.deokhugam.dashboard.repository.PopularBookRankingRepository;
import com.twogether.deokhugam.review.repository.ReviewRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PopularBookBatchService {

    private final PopularBookRankingRepository rankingRepository;
    private final ReviewRepository reviewRepository;
    @PersistenceContext
    private EntityManager em;

    @Transactional
    public void calculateAndSaveRanking(RankingPeriod period, LocalDateTime start, LocalDateTime end) {
        log.info("[{}] 인기 도서 랭킹 계산 시작 - 기간: {} ~ {}", period.name(), start, end);

        try {
            List<BookScoreDto> bookScores = reviewRepository.calculateBookScores(start, end);

            List<PopularBookRanking> rankings = bookScores.stream()
                .map(dto -> {
                    Book bookRef = em.getReference(Book.class, dto.bookId());
                    LocalDateTime now = LocalDateTime.now();
                    return PopularBookRanking.builder()
                        .book(bookRef)
                        .title(dto.title())
                        .author(dto.author())
                        .thumbnailUrl(dto.thumbnailUrl())
                        .score(dto.calculateScore())
                        .period(period)
                        .createdAt(now)
                        .build();
                })
                .toList();

            rankingRepository.saveAll(rankings);

            log.info("[{}] 인기 도서 랭킹 저장 완료 - 총 {}건", period.name(), rankings.size());
        } catch (Exception e) {
            log.error("[{}] 인기 도서 랭킹 계산 중 오류 발생", period.name(), e);
            throw e;
        }
    }

    @Transactional
    public void calculateAndSaveAllTimeRanking() {
        log.info("[역대] 인기 도서 랭킹 계산 시작");

        try {
            List<BookScoreDto> bookScores = reviewRepository.calculateBookScoresAllTime();

            List<PopularBookRanking> rankings = bookScores.stream()
                .map(dto -> {
                    Book bookRef = em.getReference(Book.class, dto.bookId());
                    LocalDateTime now = LocalDateTime.now();
                    return PopularBookRanking.builder()
                        .book(bookRef)
                        .title(dto.title())
                        .author(dto.author())
                        .thumbnailUrl(dto.thumbnailUrl())
                        .score(dto.calculateScore())
                        .period(RankingPeriod.ALL_TIME)
                        .createdAt(now)
                        .build();
                })
                .toList();

            rankingRepository.saveAll(rankings);

            log.info("[역대] 인기 도서 랭킹 저장 완료 - 총 {}건", rankings.size());
        } catch (Exception e) {
            log.error("[역대] 인기 도서 랭킹 계산 중 오류 발생", e);
            throw e;
        }
    }
}