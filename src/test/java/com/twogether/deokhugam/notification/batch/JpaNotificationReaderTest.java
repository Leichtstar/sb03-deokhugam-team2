package com.twogether.deokhugam.notification.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.twogether.deokhugam.notification.batch.reader.JpaNotificationReader;
import com.twogether.deokhugam.notification.entity.Notification;
import jakarta.persistence.EntityManagerFactory;
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
        // when
        JpaPagingItemReader<Notification> itemReader = reader.create();

        // then
        assertThat(itemReader).isNotNull();
        assertThat(itemReader.getPageSize()).isEqualTo(100);
    }
}