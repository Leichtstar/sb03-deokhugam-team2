package com.twogether.deokhugam.dashboard.batch.scheduler;

import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "batch.popular-review-ranking.enabled", havingValue = "true")
public class PopularReviewRankingScheduler {

    private final JobLauncher jobLauncher;
    private final Job popularReviewRankingJob;

    //@Scheduled(initialDelay = 1000, fixedDelay = Long.MAX_VALUE) // application 실행 시 즉시 배치 (테스트용)
    @Scheduled(cron = "${batch.popular-review-ranking.cron}")
    public void runRankingJob() {
        String jobName = "popularReviewRankingJob";
        String requestId = UUID.randomUUID().toString();

        for (RankingPeriod period : RankingPeriod.values()) {
            try {
                MDC.put("jobName", jobName);
                MDC.put("rankingPeriod", period.name());
                MDC.put("requestId", requestId);

                log.info("인기 리뷰 랭킹 배치 시작: period={}, requestId={}", period, requestId);

                JobParameters params = new JobParametersBuilder()
                    .addString("period", period.name())
                    .addString("requestId", requestId)
                    .toJobParameters();

                jobLauncher.run(popularReviewRankingJob, params);

                log.info("인기 리뷰 랭킹 배치 성공: period={}, requestId={}", period, requestId);
            } catch (Exception e) {
                log.error("인기 리뷰 랭킹 배치 실패: period={}, requestId={}", period, requestId, e);
            } finally {
                MDC.clear();
            }
        }
    }
}