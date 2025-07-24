package com.twogether.deokhugam.notification.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.twogether.deokhugam.notification.batch.reader.JpaNotificationReader;
import com.twogether.deokhugam.notification.entity.Notification;
import jakarta.persistence.EntityManagerFactory;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.database.JpaPagingItemReader;

class JpaNotificationReaderTest {

    private EntityManagerFactory entityManagerFactory;
    private JpaNotificationReader reader;

    @BeforeEach
    void setUp() {
        entityManagerFactory = mock(EntityManagerFactory.class);
        reader = new JpaNotificationReader(entityManagerFactory);
    }

    @Test
    void create_메서드는_JpaPagingItemReader를_반환한다() {
        // given
        String now = Instant.parse("2025-07-22T00:00:00Z").toString();

        // when
        JpaPagingItemReader<Notification> itemReader = reader.create(now);

        // then
        assertThat(itemReader).isNotNull();
        assertThat(itemReader.getPageSize()).isEqualTo(100);
    }
}