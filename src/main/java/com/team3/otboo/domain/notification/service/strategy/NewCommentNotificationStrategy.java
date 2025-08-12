package com.team3.otboo.domain.notification.service.strategy;

import com.team3.otboo.domain.notification.entity.Notification;
import com.team3.otboo.domain.notification.entity.NotificationLevel;
import com.team3.otboo.event.NewCommentEvent;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NewCommentNotificationStrategy implements NotificationStrategy<NewCommentEvent> {

  private final MessageSource messageSource;

  @Override
  public boolean supports(Object event) {
    return event instanceof NewCommentEvent;
  }

  @Override
  public List<Notification> createNotification(NewCommentEvent event) {
    String title = messageSource.getMessage("notification.comment.title", null, Locale.KOREAN);
    String content = messageSource.getMessage("notification.comment.content",
        new Object[]{event.commenterName()}, Locale.KOREAN);

    Notification notification = Notification.builder()
        .receiver(event.feedOwner())
        .title(title)
        .content(content)
        .level(NotificationLevel.INFO)
        .build();

    return List.of(notification);
  }
}
