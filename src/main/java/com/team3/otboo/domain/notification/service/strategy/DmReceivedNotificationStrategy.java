package com.team3.otboo.domain.notification.service.strategy;

import com.team3.otboo.domain.notification.entity.Notification;
import com.team3.otboo.domain.notification.entity.NotificationLevel;
import com.team3.otboo.event.DmReceivedEvent;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DmReceivedNotificationStrategy implements NotificationStrategy<DmReceivedEvent> {

  private final MessageSource messageSource;

  @Override
  public boolean supports(Object event) {
    return event instanceof DmReceivedEvent;
  }

  @Override
  public List<Notification> createNotification(DmReceivedEvent event) {
    String title = messageSource.getMessage("notification.dm.title", null, Locale.KOREAN);
    String content = messageSource.getMessage("notification.dm.content",
        new Object[]{event.senderName()}, Locale.KOREAN);

    Notification notification = Notification.builder()
        .receiver(event.receiver())
        .title(title)
        .content(content)
        .level(NotificationLevel.INFO)
        .build();
    return List.of(notification);
  }
}
