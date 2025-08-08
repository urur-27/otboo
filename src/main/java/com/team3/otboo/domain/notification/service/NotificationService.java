package com.team3.otboo.domain.notification.service;

import com.team3.otboo.domain.notification.dto.NotificationDto;
import com.team3.otboo.domain.notification.entity.Notification;
import com.team3.otboo.domain.notification.entity.NotificationLevel;
import com.team3.otboo.domain.notification.mapper.NotificationMapper;
import com.team3.otboo.domain.notification.repository.NotificationRepository;
import com.team3.otboo.domain.user.entity.User;
import com.team3.otboo.event.NewFollowerEvent;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

  private final NotificationRepository notificationRepository;
  private final SseService sseService;
  private final NotificationMapper notificationMapper;

  @EventListener
  @Transactional
  public void handleNewFollowerEvent(NewFollowerEvent event) {
    Notification notification = Notification.builder()
        .receiver(event.followee())
        .title("새로운 팔로워")
        .content(event.followerName() + "님이 회원님을 팔로우하기 시작했습니다.")
        .level(NotificationLevel.INFO)
        .build();

    notificationRepository.save(notification);

    NotificationDto notificationDto = notificationMapper.toDto(notification);

    sseService.sendNotification(event.followee().getId(), notificationDto);
  }

  public List<NotificationDto> findNotificationsByUser(User user) {
    List<Notification> notifications = notificationRepository.findByReceiverOrderByCreatedAtDescIdDesc(user);
    return notificationMapper.toDtoList(notifications);
  }
}
