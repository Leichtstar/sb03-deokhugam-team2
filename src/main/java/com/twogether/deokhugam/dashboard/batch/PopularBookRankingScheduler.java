package com.twogether.deokhugam.dashboard.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Scheduled(cron = "0 0 0 * * *") // 매일 자정 실행
    public void runPopularBookRankingJob() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                .addString("period", "DAILY") // 여기서 WEEKLY 등으로 바꾸면 확장 가능
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

            jobLauncher.run(popularBookRankingJob, jobParameters);

            log.info("인기 도서 랭킹 배치 Job 실행 완료");
        } catch (Exception e) {
            log.error("인기 도서 랭킹 배치 Job 실행 실패", e);
        }
    }
}
