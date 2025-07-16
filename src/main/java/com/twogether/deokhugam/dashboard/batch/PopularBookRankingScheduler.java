package com.twogether.deokhugam.dashboard.batch;

import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PopularBookRankingScheduler {

    private final JobLauncher jobLauncher;
    private final Job popularBookRankingJob;

    @Scheduled(cron = "0 0 0 * * *") // 매일 자정
    public void runRankingJob() {
        String jobName = "popularBookRankingJob";
        String requestId = UUID.randomUUID().toString();

        for (RankingPeriod period : RankingPeriod.values()) {
            try {
                // MDC 로그 컨텍스트 설정
                MDC.put("jobName", jobName);
                MDC.put("rankingPeriod", period.name());
                MDC.put("requestId", requestId);

                log.info("인기 도서 랭킹 배치 시작: period={}, requestId={}", period, requestId);

                JobParameters params = new JobParametersBuilder()
                    .addString("period", period.name())
                    .addString("requestId", requestId)
                    .toJobParameters();

                jobLauncher.run(popularBookRankingJob, params);

                log.info("인기 도서 랭킹 배치 성공: period={}, requestId={}", period, requestId);
            } catch (Exception e) {
                log.error("인기 도서 랭킹 배치 실패: period={}, requestId={}", period, requestId, e);
            } finally {
                // 컨텍스트 제거
                MDC.clear();
            }
        }
    }
}