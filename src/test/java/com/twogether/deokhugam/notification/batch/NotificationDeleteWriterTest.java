package com.twogether.deokhugam.notification.batch;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.twogether.deokhugam.notification.batch.writer.NotificationDeleteWriter;
import com.twogether.deokhugam.notification.entity.Notification;
import com.twogether.deokhugam.notification.repository.NotificationRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.batch.item.Chunk;

class NotificationDeleteWriterTest {

    @Mock
    private NotificationRepository notificationRepository;

    private NotificationDeleteWriter writer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        writer = new NotificationDeleteWriter(notificationRepository);
    }

    @Test
    void 알림이_있으면_일괄삭제한다() throws Exception {
        // given
        Notification n1 = mock(Notification.class);
        Notification n2 = mock(Notification.class);
        Chunk<Notification> chunk = new Chunk<>(List.of(n1, n2));

        // when
        writer.write(chunk);

        // then
        verify(notificationRepository).deleteAllInBatch(List.of(n1, n2));
    }

    @Test
    void 알림이_없으면_삭제하지_않는다() throws Exception {
        // given
        Chunk<Notification> chunk = new Chunk<>(List.of());

        // when
        writer.write(chunk);

        // then
        verify(notificationRepository, never()).deleteAllInBatch(any());
    }
}