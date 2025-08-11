package com.team3.otboo.domain.notification.service.strategy;

import com.team3.otboo.domain.notification.entity.Notification;
import java.util.List;

public interface NotificationStrategy<E>{

  boolean supports(Object event);

  List<Notification> createNotification(E event);
}
