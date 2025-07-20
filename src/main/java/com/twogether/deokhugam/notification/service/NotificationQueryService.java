package com.twogether.deokhugam.notification.service;

import com.twogether.deokhugam.common.dto.CursorPageResponse;
import com.twogether.deokhugam.notification.dto.NotificationDto;
import com.twogether.deokhugam.notification.entity.Notification;
import com.twogether.deokhugam.notification.mapper.NotificationMapper;
import com.twogether.deokhugam.notification.repository.NotificationRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationQueryService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    public CursorPageResponse<NotificationDto> getNotifications(
        UUID userId,
        String cursor,
        LocalDateTime after,
        Integer limit,
        Sort.Direction direction
    ) {
        int pageSize = (limit != null && limit > 0) ? limit : 20;
        Sort.Direction sortDirection = (direction != null) ? direction : Sort.Direction.DESC;

        PageRequest pageable = PageRequest.of(0, pageSize, Sort.by(sortDirection, "createdAt"));

        LocalDateTime parsedCursor = null;
        if (cursor != null && !cursor.isBlank()) {
            try {
                parsedCursor = LocalDateTime.parse(cursor);
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid cursor format. Use ISO-8601 date-time format.");
            }
        }

        List<Notification> notifications = notificationRepository.findByUserIdWithCursor(
            userId,
            parsedCursor,
            pageable
        );

        List<NotificationDto> content = notifications.stream()
            .map(notificationMapper::toDto)
            .toList();

        boolean hasNext = content.size() == pageSize;
        String nextCursor = hasNext ? content.get(content.size() - 1).createdAt().toString() : null;
        LocalDateTime nextAfter = hasNext ? content.get(content.size() - 1).createdAt() : null;

        return new CursorPageResponse<>(content, nextCursor, nextAfter, pageSize, hasNext);
    }
}