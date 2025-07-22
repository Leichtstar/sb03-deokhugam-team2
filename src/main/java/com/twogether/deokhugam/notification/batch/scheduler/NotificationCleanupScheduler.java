package com.twogether.deokhugam.notification.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationCleanupScheduler {

    private final JobLauncher jobLauncher;
    private final Job notificationCleanupJob;
    
    @Scheduled(cron = "0 0 0 * * *")
    public void runNotificationCleanupJob() {
        try {
            log.info("[Batch] 읽은 알림 삭제 배치 시작");
            jobLauncher.run(notificationCleanupJob, new JobParameters());
            log.info("[Batch] 읽은 알림 삭제 배치 완료");
        } catch (Exception e) {
            log.error("[Batch] 읽은 알림 삭제 배치 실패", e);
        }
    }
}