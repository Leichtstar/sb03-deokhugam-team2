package com.twogether.deokhugam.notification.batch.scheduler;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "batch.notification-cleanup.enabled", havingValue = "true")
public class NotificationCleanupScheduler {

    private final JobLauncher jobLauncher;
    private final Job notificationCleanupJob;

    @Scheduled(cron = "${batch.notification-cleanup.cron:0 15 0 * * *}")
    public void runNotificationCleanupJob() {
        try {
            log.info("[NotificationCleanupScheduler] 읽은 알림 삭제 배치 시작");

            JobParameters jobParameters = new JobParametersBuilder()
                .addString("now", Instant.now().toString())
                .toJobParameters();

            JobExecution execution = jobLauncher.run(notificationCleanupJob, jobParameters);

            if (execution.getStatus() == BatchStatus.COMPLETED) {
                log.info("[NotificationCleanupScheduler] 읽은 알림 삭제 배치 성공");
            } else {
                log.error("[NotificationCleanupScheduler] 읽은 알림 삭제 배치 실패: status={}", execution.getStatus());
            }

        } catch (Exception e) {
            log.error("[NotificationCleanupScheduler] 읽은 알림 삭제 배치 실행 중 예외 발생", e);
        }
    }
}