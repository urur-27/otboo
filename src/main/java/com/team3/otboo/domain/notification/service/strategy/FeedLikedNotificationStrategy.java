package com.team3.otboo.domain.notification.service.strategy;

import com.team3.otboo.domain.notification.entity.Notification;
import com.team3.otboo.domain.notification.entity.NotificationLevel;
import com.team3.otboo.event.FeedLikedEvent;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeedLikedNotificationStrategy implements NotificationStrategy<FeedLikedEvent>{

  private final MessageSource messageSource;

  @Override
  public boolean supports(Object event) {
    return event instanceof FeedLikedEvent;
  }

  @Override
  public List<Notification> createNotification(FeedLikedEvent event) {
    String title = messageSource.getMessage("notification.like.title", null, Locale.KOREAN);
    String content = messageSource.getMessage("notification.like.content",
        new Object[]{event.likerName()}, Locale.KOREAN);

    Notification notification = Notification.builder()
        .receiver(event.feedOwner())
        .title(title)
        .content(content)
        .level(NotificationLevel.INFO)
        .build();

    return List.of(notification);
  }
}
