package com.twogether.deokhugam.dashboard.batch;

import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PopularBookRankingScheduler {

    private final PopularBookBatchService popularBookBatchService;

    @Scheduled(cron = "0 0 0 * * *")
    public void runAllPopularBookRankingBatches() {
        log.info("인기 도서 랭킹 배치 시작");

        try {
            // 기준 시각: 오늘 00시
            LocalDateTime end = LocalDate.now().atStartOfDay();

            // 일간
            LocalDateTime dailyStart = end.minusDays(1);
            popularBookBatchService.calculateAndSaveRanking(RankingPeriod.DAILY, dailyStart, end);

            // 주간
            LocalDateTime weeklyStart = end.minusDays(7);
            popularBookBatchService.calculateAndSaveRanking(RankingPeriod.WEEKLY, weeklyStart, end);

            // 월간
            LocalDateTime monthlyStart = end.minusDays(30);
            popularBookBatchService.calculateAndSaveRanking(RankingPeriod.MONTHLY, monthlyStart, end);

            // 역대
            popularBookBatchService.calculateAndSaveAllTimeRanking();

            log.info("인기 도서 랭킹 배치 완료");
        } catch (Exception e) {
            log.error("인기 도서 랭킹 배치 실패", e);
        }
    }
}
