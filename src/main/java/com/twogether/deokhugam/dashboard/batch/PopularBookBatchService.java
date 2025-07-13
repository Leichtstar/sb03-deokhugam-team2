package com.twogether.deokhugam.dashboard.batch;

import com.twogether.deokhugam.dashboard.entity.PopularBookRanking;
import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import com.twogether.deokhugam.dashboard.repository.PopularBookRankingRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PopularBookBatchService {

    private final PopularBookRankingRepository rankingRepository;

    public void calculateAndSaveRanking(RankingPeriod period, LocalDateTime start, LocalDateTime end) {
        // ReviewRepository에서 BookScoreDto 리스트 받기
        // List<BookScoreDto> scores = reviewRepository.calculateScores(start, end);

        // 더미처리
        List<PopularBookRanking> rankings = List.of();

        rankingRepository.saveAll(rankings);
    }

    public void runDailyBatchNow() {
        RankingPeriod period = RankingPeriod.DAILY;
        LocalDateTime start = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = start.plusDays(1);

        calculateAndSaveRanking(period, start, end);
    }
}
