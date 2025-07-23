package com.twogether.deokhugam.notification.batch.reader;

import com.twogether.deokhugam.notification.entity.Notification;
import jakarta.persistence.EntityManagerFactory;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.stereotype.Component;

@Component
public class JpaNotificationReader {

    private final EntityManagerFactory entityManagerFactory;

    public JpaNotificationReader(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    public JpaPagingItemReader<Notification> create() {
        // 테스트용 15분 기준 삭제로 변경
        LocalDateTime cutoff = LocalDateTime.now().minus(15, ChronoUnit.MINUTES);
        // 실제 배포 환경 사용
        //LocalDateTime cutoff = LocalDateTime.now().minus(7, ChronoUnit.DAYS);

        Map<String, Object> params = new HashMap<>();
        params.put("cutoff", cutoff);

        return new JpaPagingItemReaderBuilder<Notification>()
            .name("jpaNotificationReader")
            .entityManagerFactory(entityManagerFactory)
            .queryString("""
                    SELECT n FROM Notification n
                    WHERE n.confirmed = true AND n.updatedAt < :cutoff
                """)
            .parameterValues(params)
            .pageSize(100)
            .build();
    }
}