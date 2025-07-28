package com.twogether.deokhugam.notification.batch;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import com.twogether.deokhugam.notification.batch.writer.NotificationDeleteWriter;
import com.twogether.deokhugam.notification.entity.Notification;
import com.twogether.deokhugam.notification.repository.NotificationRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.Chunk;

@DisplayName("NotificationDeleteWriter 단위 테스트")
class NotificationDeleteWriterTest {

    private NotificationRepository notificationRepository;
    private MeterRegistry meterRegistry;
    private NotificationDeleteWriter writer;

    @BeforeEach
    void setUp() {
        notificationRepository = mock(NotificationRepository.class);
        meterRegistry = spy(new SimpleMeterRegistry());
        writer = new NotificationDeleteWriter(notificationRepository, meterRegistry);
    }

    @Test
    @DisplayName("알림이 있으면 일괄 삭제하고 메트릭을 증가시킨다")
    void 알림이_있으면_일괄삭제하고_메트릭_증가() throws Exception {
        // given
        Notification n1 = mock(Notification.class);
        Notification n2 = mock(Notification.class);
        Chunk<Notification> chunk = new Chunk<>(List.of(n1, n2));

        // when
        writer.write(chunk);

        // then
        verify(notificationRepository).deleteAllInBatch(List.of(n1, n2));

        double count = meterRegistry.get("batch.notification.cleaned.count").counter().count();
        assert count == 2.0;
    }

    @Test
    @DisplayName("알림이 없으면 삭제하지 않고 메트릭도 증가하지 않는다")
    void 알림이_없으면_삭제하지_않고_메트릭도_증가하지_않는다() throws Exception {
        // given
        Chunk<Notification> chunk = new Chunk<>(List.of());

        // when
        writer.write(chunk);

        // then
        verify(notificationRepository, never()).deleteAllInBatch(any());
        assert meterRegistry.find("batch.notification.cleaned.count").counter() == null;
    }

    @Test
    @DisplayName("삭제 중 예외가 발생하면 실패 메트릭을 증가시킨다")
    void 삭제_중_예외_발생하면_실패_메트릭_증가() {
        // given
        Notification n1 = mock(Notification.class);
        Chunk<Notification> chunk = new Chunk<>(List.of(n1));

        doThrow(new RuntimeException("DB 오류"))
            .when(notificationRepository).deleteAllInBatch(any());

        // when & then
        try {
            writer.write(chunk);
        } catch (Exception ignored) {
        }

        double failed = meterRegistry.get("batch.notification.cleaned.count.failed").counter().count();
        assert failed == 1.0;
    }
}