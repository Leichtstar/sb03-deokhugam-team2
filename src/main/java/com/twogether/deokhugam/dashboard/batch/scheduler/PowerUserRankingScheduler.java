package com.twogether.deokhugam.dashboard.batch.scheduler;

import com.twogether.deokhugam.dashboard.entity.RankingPeriod;
import com.twogether.deokhugam.dashboard.repository.PowerUserRankingRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "batch.power-user-ranking.enabled", havingValue = "true")
public class PowerUserRankingScheduler {

    private final JobLauncher jobLauncher;
    private final Job powerUserRankingJob;
    private final PowerUserRankingRepository powerUserRankingRepository;

    //@Scheduled(initialDelay = 1000, fixedDelay = Long.MAX_VALUE) // application 실행 시 즉시 배치 (테스트용)
    @Scheduled(cron = "${batch.power-user-ranking.cron}")
    public void runRankingJob() {
        String jobName = "powerUserRankingJob";
        String requestId = UUID.randomUUID().toString();
        int successCount = 0;
        int failureCount = 0;

        for (RankingPeriod period : RankingPeriod.values()) {
            try {
                MDC.put("jobName", jobName);
                MDC.put("rankingPeriod", period.name());
                MDC.put("requestId", requestId);

                log.info("파워 유저 랭킹 배치 시작: period={}, requestId={}", period, requestId);
                powerUserRankingRepository.deleteByPeriod(period);

                JobParameters params = new JobParametersBuilder()
                    .addString("period", period.name())
                    .addString("requestId", requestId)
                    .toJobParameters();

                jobLauncher.run(powerUserRankingJob, params);

                log.info("파워 유저 랭킹 배치 성공: period={}, requestId={}", period, requestId);
                successCount++;
            } catch (Exception e) {
                log.error("파워 유저 랭킹 배치 실패: period={}, requestId={}", period, requestId, e);
                failureCount++;
            } finally {
                MDC.clear();
            }
        }
        log.info("파워 유저 랭킹 배치 전체 완료: 성공={}, 실패={}, requestId={}",
            successCount, failureCount, requestId);
    }
}