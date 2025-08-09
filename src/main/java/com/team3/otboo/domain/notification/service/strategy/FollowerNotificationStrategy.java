package com.team3.otboo.domain.notification.service.strategy;

import com.team3.otboo.domain.notification.entity.Notification;
import com.team3.otboo.domain.notification.entity.NotificationLevel;
import com.team3.otboo.event.NewFollowerEvent;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FollowerNotificationStrategy implements NotificationStrategy<NewFollowerEvent> {

  private final MessageSource messageSource;

  @Override
  public boolean supports(Object event) {
    return event instanceof NewFollowerEvent;
  }

  @Override
  public List<Notification> createNotification(NewFollowerEvent event) {
    String title = messageSource.getMessage("notification.follower.title", null, Locale.KOREAN);
    String content = messageSource.getMessage("notification.follower.content",
        new Object[]{event.followerName()},
        Locale.KOREAN);

    Notification notification = Notification.builder()
        .receiver(event.followee())
        .title(title)
        .content(content)
        .level(NotificationLevel.INFO)
        .build();

    return List.of(notification);
  }
}
