package com.twogether.deokhugam.notification.mapper;

import com.twogether.deokhugam.notification.dto.NotificationDto;
import com.twogether.deokhugam.notification.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationDto toDto(Notification notification) {
        return new NotificationDto(
            notification.getId(),
            notification.getUser().getId(),
            notification.getReview().getId(),
            notification.getReview().getBook().getTitle(),
            notification.getContent(),
            notification.isConfirmed(),
            notification.getCreatedAt(),
            notification.getUpdatedAt()
        );
    }
}