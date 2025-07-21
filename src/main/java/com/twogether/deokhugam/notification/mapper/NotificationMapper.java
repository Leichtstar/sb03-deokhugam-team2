package com.twogether.deokhugam.notification.mapper;

import com.twogether.deokhugam.notification.dto.NotificationDto;
import com.twogether.deokhugam.notification.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationDto toDto(Notification notification) {
        if (notification == null || notification.getReview() == null ||
            notification.getReview().getBook() == null || notification.getUser() == null) {
            throw new IllegalArgumentException("필수 엔티티가 null입니다");
        }

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