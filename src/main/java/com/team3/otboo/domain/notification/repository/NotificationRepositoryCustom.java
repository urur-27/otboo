package com.team3.otboo.domain.notification.repository;

import com.team3.otboo.domain.notification.dto.NotificationSearchCondition;
import com.team3.otboo.domain.notification.entity.Notification;
import com.team3.otboo.domain.user.entity.User;
import java.util.List;
import java.util.UUID;

public interface NotificationRepositoryCustom {

  List<Notification> findByReceiverWithCursor(User receiver, NotificationSearchCondition condition);
}
