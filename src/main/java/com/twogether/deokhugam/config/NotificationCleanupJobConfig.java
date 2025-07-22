package com.twogether.deokhugam.config;

import com.twogether.deokhugam.notification.batch.reader.JpaNotificationReader;
import com.twogether.deokhugam.notification.batch.writer.NotificationDeleteWriter;
import com.twogether.deokhugam.notification.entity.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class NotificationCleanupJobConfig {

    private final JpaNotificationReader jpaNotificationReader;
    private final NotificationDeleteWriter notificationDeleteWriter;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job notificationCleanupJob(JobRepository jobRepository) {
        return new JobBuilder("notificationCleanupJob", jobRepository)
            .start(notificationCleanupStep(jobRepository))
            .build();
    }

    @Bean
    @JobScope
    public Step notificationCleanupStep(JobRepository jobRepository) {
        return new StepBuilder("notificationCleanupStep", jobRepository)
            .<Notification, Notification>chunk(100, transactionManager)
            .reader(notificationReader())
            .writer(notificationDeleteWriter)
            .build();
    }

    @Bean
    public JpaPagingItemReader<Notification> notificationReader() {
        return jpaNotificationReader.create();
    }
}