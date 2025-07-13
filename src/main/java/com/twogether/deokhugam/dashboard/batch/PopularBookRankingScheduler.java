package com.twogether.deokhugam.dashboard.batch;

import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PopularBookRankingScheduler {

    private final PopularBookBatchService popularBookBatchService;

    @Scheduled(cron = "0 0 0 * * *")
    public void runDailyPopularBookRankingBatch() {
        RankingPeriod period = RankingPeriod.DAILY;

        LocalDateTime start = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = start.plusDays(1);

        popularBookBatchService.calculateAndSaveRanking(period, start, end);
    }
}
