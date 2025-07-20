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
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("prod")
public class PowerUserRankingScheduler {

    private final JobLauncher jobLauncher;
    private final Job powerUserRankingJob;
    private final PowerUserRankingRepository powerUserRankingRepository;

    @Scheduled(cron = "0 0 0 * * *") // 매일 자정
    public void runRankingJob() {
        String jobName = "powerUserRankingJob";
        String requestId = UUID.randomUUID().toString();

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
            } catch (Exception e) {
                log.error("파워 유저 랭킹 배치 실패: period={}, requestId={}", period, requestId, e);
            } finally {
                MDC.clear();
            }
        }
    }
}