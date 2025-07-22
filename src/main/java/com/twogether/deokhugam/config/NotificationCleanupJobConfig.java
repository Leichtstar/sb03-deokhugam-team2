package com.twogether.deokhugam.config;

import com.twogether.deokhugam.notification.entity.Notification;
import com.twogether.deokhugam.notification.repository.NotificationRepository;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class NotificationCleanupJobConfig {

    private final NotificationRepository notificationRepository;
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
            .writer(notificationWriter())
            .build();
    }

    @Bean
    public JpaPagingItemReader<Notification> notificationReader() {
        LocalDateTime cutoff = LocalDateTime.now().minus(7, ChronoUnit.DAYS);

        Map<String, Object> params = new HashMap<>();
        params.put("cutoff", cutoff);

        return new JpaPagingItemReaderBuilder<Notification>()
            .name("notificationReader")
            .entityManagerFactory(notificationRepository.getEntityManager().getEntityManagerFactory())
            .queryString("""
                    SELECT n FROM Notification n
                    WHERE n.confirmed = true AND n.updatedAt < :cutoff
                """)
            .parameterValues(params)
            .pageSize(100)
            .build();
    }

    @Bean
    public ItemWriter<Notification> notificationWriter() {
        return notifications -> notificationRepository.deleteAllInBatch(notifications);
    }
}