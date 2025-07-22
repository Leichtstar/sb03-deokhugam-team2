package com.twogether.deokhugam.notification.repository;

import com.twogether.deokhugam.notification.entity.Notification;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    // after가 있는 경우
    @Query("""
        SELECT n FROM Notification n
        WHERE n.user.id = :userId AND n.createdAt < :after
        ORDER BY n.createdAt DESC, n.id DESC
    """)
    List<Notification> findByUserIdWithAfter(
        @Param("userId") UUID userId,
        @Param("after") LocalDateTime after,
        Pageable pageable
    );

    // after가 없는 경우
    @Query("""
        SELECT n FROM Notification n
        WHERE n.user.id = :userId
        ORDER BY n.createdAt DESC, n.id DESC
    """)
    List<Notification> findByUserIdWithoutAfter(
        @Param("userId") UUID userId,
        Pageable pageable
    );

    Optional<Notification> findByIdAndUserId(UUID id, UUID userId);
}