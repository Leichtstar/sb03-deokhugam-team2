package com.twogether.deokhugam.notification.batch.writer;

import com.twogether.deokhugam.notification.entity.Notification;
import com.twogether.deokhugam.notification.repository.NotificationRepository;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationDeleteWriter implements ItemWriter<Notification> {

    private final NotificationRepository notificationRepository;
    private final MeterRegistry meterRegistry;

    @Override
    public void write(Chunk<? extends Notification> chunk) {
        if (chunk.isEmpty()) {
            log.info("🔕 알림 삭제 스킵 - 삭제할 알림 없음");
            return;
        }

        List<Notification> notifications = new ArrayList<>(chunk.getItems());

        try {
            notificationRepository.deleteAllInBatch(notifications);

            // 커스텀 메트릭 기록
            meterRegistry.counter("batch.notification.cleaned.count")
                .increment(notifications.size());

            log.info("🔔 알림 {}건 삭제 완료", notifications.size());

        } catch (Exception e) {
            log.error("🚨 알림 삭제 중 오류 발생", e);

            // 실패 건 메트릭 기록
            meterRegistry.counter("batch.notification.cleaned.count.failed").increment();

            throw e;
        }
    }
}