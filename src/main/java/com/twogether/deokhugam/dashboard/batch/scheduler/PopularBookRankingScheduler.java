package com.twogether.deokhugam.dashboard.batch.scheduler;

import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "batch.popular-book-ranking.enabled", havingValue = "true")
public class PopularBookRankingScheduler {

    private final JobLauncher jobLauncher;
    private final Job popularBookRankingJob;

    //@Scheduled(initialDelay = 1000, fixedDelay = Long.MAX_VALUE) // application 실행 시 즉시 배치 (테스트용)
    @Scheduled(cron = "${batch.popular-book-ranking.cron:0 0 0 * * *}")
    public void runRankingJob() {
        String jobName = "popularBookRankingJob";
        String requestId = UUID.randomUUID().toString();

        for (RankingPeriod period : RankingPeriod.values()) {
            try {
                MDC.put("jobName", jobName);
                MDC.put("rankingPeriod", period.name());
                MDC.put("requestId", requestId);

                log.info("인기 도서 랭킹 배치 시작: period={}, requestId={}", period, requestId);

                JobParameters params = new JobParametersBuilder()
                    .addString("period", period.name())
                    .addString("now", Instant.now().toString())
                    .addString("requestId", requestId)
                    .toJobParameters();

                JobExecution jobExecution = jobLauncher.run(popularBookRankingJob, params);

                if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
                    log.info("인기 도서 랭킹 배치 성공: period={}, requestId={}", period, requestId);
                } else {
                    log.error("인기 도서 랭킹 배치 실패: period={}, requestId={}, status={}",
                        period, requestId, jobExecution.getStatus());
                }

            } catch (Exception e) {
                log.error("인기 도서 랭킹 배치 실패: period={}, requestId={}", period, requestId, e);
            } finally {
                MDC.clear();
            }
        }
    }
}