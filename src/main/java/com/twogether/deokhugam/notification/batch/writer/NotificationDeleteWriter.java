package com.twogether.deokhugam.notification.batch.writer;

import com.twogether.deokhugam.notification.entity.Notification;
import com.twogether.deokhugam.notification.repository.NotificationRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationDeleteWriter implements ItemWriter<Notification> {

    private final NotificationRepository notificationRepository;

    @Override
    public void write(Chunk<? extends Notification> chunk) {
        if (!chunk.isEmpty()) {
            List<Notification> notifications = new ArrayList<>(chunk.getItems());
            notificationRepository.deleteAllInBatch(notifications);
        }
    }
}