package com.twogether.deokhugam.notification.batch;

import static org.assertj.core.api.Assertions.assertThat;

import com.twogether.deokhugam.notification.entity.Notification;
import com.twogether.deokhugam.notification.repository.NotificationRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest
@SpringBatchTest
@ActiveProfiles("test")
@Sql(scripts = "/sql/notification_cleanup_test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class NotificationCleanupJobTest {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job notificationCleanupJob;

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    void 읽은_알림이_7일_지난_경우_삭제된다() throws Exception {
        // given
        JobParameters params = new JobParametersBuilder()
            .addString("now", "2025-07-22T00:00:00Z")
            .toJobParameters();

        // when
        JobExecution execution = jobLauncher.run(notificationCleanupJob, params);

        // then
        List<Notification> remaining = notificationRepository.findAll();
        assertThat(remaining)
            .hasSize(1)
            .extracting(n -> n.getId().toString())
            .containsExactly("44444444-4444-4444-4444-000000000002");

    }
}