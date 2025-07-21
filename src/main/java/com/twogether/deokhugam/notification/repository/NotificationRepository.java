package com.twogether.deokhugam.notification.repository;

import com.twogether.deokhugam.notification.entity.Notification;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

}