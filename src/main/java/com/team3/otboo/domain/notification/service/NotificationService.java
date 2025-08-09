package com.team3.otboo.domain.notification.service;

import com.team3.otboo.domain.notification.dto.NotificationDto;
import com.team3.otboo.domain.notification.entity.Notification;
import com.team3.otboo.domain.notification.entity.NotificationLevel;
import com.team3.otboo.domain.notification.mapper.NotificationMapper;
import com.team3.otboo.domain.notification.repository.NotificationRepository;
import com.team3.otboo.domain.notification.service.strategy.NotificationStrategy;
import com.team3.otboo.domain.user.entity.User;
import com.team3.otboo.domain.user.repository.UserRepository;
import com.team3.otboo.event.FeedLikedEvent;
import com.team3.otboo.event.NewFollowerEvent;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

  private final NotificationRepository notificationRepository;
  private final UserRepository userRepository;
  private final SseService sseService;
  private final NotificationMapper notificationMapper;
  private final List<NotificationStrategy<?>> strategies;

  @EventListener
  @Transactional
  public void handleEvent(Object event) {
    strategies.stream()
        .filter(strategy -> strategy.supports(event))
        .findFirst()
        .ifPresent(strategy -> processNotification(strategy, event));
  }

  @SuppressWarnings("unchecked")
  private <E> void processNotification(NotificationStrategy<E> strategy, Object event) {
    List<Notification> notifications = strategy.createNotification((E) event);

    for (Notification notification : notifications) {
      notificationRepository.save(notification);
      NotificationDto dto = notificationMapper.toDto(notification);
      sseService.sendNotification(notification.getReceiver().getId(), dto);
    }
  }

  @Transactional(readOnly = true)
  public List<NotificationDto> findNotificationsByUser(User user) {
    List<Notification> notifications = notificationRepository.findByReceiverOrderByCreatedAtDescIdDesc(user);
    return notificationMapper.toDtoList(notifications);
  }
}
