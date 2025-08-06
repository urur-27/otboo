package com.team3.otboo.domain.notification.repository;

import com.team3.otboo.domain.notification.entity.Notification;
import com.team3.otboo.domain.user.entity.User;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

  List<Notification> findByReceiverOrderByCreatedAtDescIdDesc(User receiver);
}
