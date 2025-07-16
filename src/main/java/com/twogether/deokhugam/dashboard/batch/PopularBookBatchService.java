package com.twogether.deokhugam.dashboard.batch;

import com.twogether.deokhugam.book.entity.Book;
import com.twogether.deokhugam.dashboard.batch.model.BookScoreDto;
import com.twogether.deokhugam.dashboard.entity.PopularBookRanking;
import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import com.twogether.deokhugam.dashboard.repository.PopularBookRankingRepository;
import com.twogether.deokhugam.review.repository.ReviewRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PopularBookBatchService {

    private final PopularBookRankingRepository rankingRepository;
    private final ReviewRepository reviewRepository;

    @PersistenceContext
    private EntityManager em;

    @Value("${batch.ranking.batch-size:1000}")
    private int batchSize;

    public void calculateAndSaveDailyRanking() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = now.withHour(23).withMinute(59).withSecond(59).withNano(999_999_999);
        calculateAndSaveRanking(RankingPeriod.DAILY, start, end);
    }

    @Transactional
    public void calculateAndSaveRanking(RankingPeriod period, LocalDateTime start, LocalDateTime end) {
        log.info("[{}] 인기 도서 랭킹 계산 시작 - 기간: {} ~ {}", period.name(), start, end);

        try {
            rankingRepository.deleteByPeriod(period);
            List<BookScoreDto> bookScores = reviewRepository.calculateBookScores(start, end);
            List<PopularBookRanking> rankings = createRankings(bookScores, period);
            saveInBatch(rankings);

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
            rankingRepository.deleteByPeriod(RankingPeriod.ALL_TIME);

            List<BookScoreDto> bookScores = reviewRepository.calculateBookScoresAllTime();
            List<PopularBookRanking> rankings = createRankings(bookScores, RankingPeriod.ALL_TIME);

            saveInBatch(rankings);

            log.info("[역대] 인기 도서 랭킹 저장 완료 - 총 {}건", rankings.size());
        } catch (Exception e) {
            log.error("[역대] 인기 도서 랭킹 계산 중 오류 발생", e);
            throw e;
        }
    }

    private List<PopularBookRanking> createRankings(List<BookScoreDto> bookScores, RankingPeriod period) {
        return bookScores.stream()
            .map(dto -> {
                Book bookRef = em.find(Book.class, dto.bookId());
                if (bookRef == null) {
                    log.warn("Book not found: {}", dto.bookId());
                    return null;
                }
                return PopularBookRanking.builder()
                    .book(bookRef)
                    .title(dto.title())
                    .author(dto.author())
                    .thumbnailUrl(dto.thumbnailUrl())
                    .score(dto.calculateScore())
                    .period(period)
                    .createdAt(LocalDateTime.now())
                    .build();
            })
            .filter(Objects::nonNull)
            .toList();
    }

    private void saveInBatch(List<PopularBookRanking> rankings) {
        for (int i = 0; i < rankings.size(); i += batchSize) {
            int end = Math.min(i + batchSize, rankings.size());
            List<PopularBookRanking> batch = rankings.subList(i, end);
            rankingRepository.saveAll(batch);
            em.flush();
            em.clear();
        }
    }
}