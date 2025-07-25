package com.twogether.deokhugam.notification.service;

import com.twogether.deokhugam.common.dto.CursorPageResponse;
import com.twogether.deokhugam.notification.dto.NotificationDto;
import com.twogether.deokhugam.notification.entity.Notification;
import com.twogether.deokhugam.notification.mapper.NotificationMapper;
import com.twogether.deokhugam.notification.repository.NotificationRepository;
import java.time.Instant;
import java.time.format.DateTimeParseException;
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
        Instant after,
        Integer limit,
        Sort.Direction direction
    ) {
        int pageSize = (limit != null && limit > 0 && limit <= 100) ? limit : 20;
        Sort.Direction sortDirection = (direction != null) ? direction : Sort.Direction.DESC;

        // 실제 데이터보다 1개 더 조회하여 다음 페이지 존재 여부 확인
        PageRequest pageable = PageRequest.of(0, pageSize + 1, Sort.by(sortDirection, "createdAt", "id"));

        Instant parsedCursor = null;
        if (cursor != null && !cursor.isBlank()) {
            try {
                parsedCursor = Instant.parse(cursor);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid cursor format. Use ISO-8601 date-time format.", e);
            }
        }

        List<Notification> notifications;

        // cursor와 after 모두 있는 경우
        if (parsedCursor != null && after != null) {
            notifications = notificationRepository.findByUserIdWithCursorAndAfter(userId, parsedCursor, after, pageable);
        }
        // cursor만 있는 경우
        else if (parsedCursor != null) {
            notifications = notificationRepository.findByUserIdWithCursor(userId, parsedCursor, pageable);
        }
        // after만 있는 경우
        else if (after != null) {
            notifications = notificationRepository.findByUserIdWithAfter(userId, after, pageable);
        }
        // 기본 조회
        else {
            notifications = notificationRepository.findByUserIdWithoutAfter(userId, pageable);
        }

        List<NotificationDto> content = notifications.stream()
            .map(notificationMapper::toDto)
            .toList();

        // hasNext 계산 및 결과 조정
        boolean hasNext = content.size() > pageSize;
        if (hasNext) {
            content = content.subList(0, pageSize);
        }

        String nextCursor = hasNext ? content.get(content.size() - 1).createdAt().toString() : null;
        Instant nextAfter = hasNext ? content.get(content.size() - 1).createdAt() : null;

        return new CursorPageResponse<>(content, nextCursor, nextAfter, pageSize, hasNext);
    }
}