package com.twogether.deokhugam.notification.repository;

import com.twogether.deokhugam.notification.entity.Notification;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    @Query("""
    SELECT n FROM Notification n
    WHERE n.user.id = :userId
    AND (:after IS NULL OR n.createdAt < :after)
    ORDER BY n.createdAt DESC
    """)
    List<Notification> findByUserIdWithCursor(
        @Param("userId") UUID userId,
        @Param("after") LocalDateTime after,
        Pageable pageable
    );
}