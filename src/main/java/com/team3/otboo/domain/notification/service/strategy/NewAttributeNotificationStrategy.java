package com.team3.otboo.domain.notification.service.strategy;

import com.team3.otboo.domain.notification.entity.Notification;
import com.team3.otboo.domain.notification.entity.NotificationLevel;
import com.team3.otboo.domain.user.entity.User;
import com.team3.otboo.domain.user.repository.UserRepository;
import com.team3.otboo.event.NewAttributeAddedEvent;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NewAttributeNotificationStrategy implements NotificationStrategy<NewAttributeAddedEvent> {

  private final UserRepository userRepository;
  private final MessageSource messageSource;

  @Override
  public boolean supports(Object event) {
    return event instanceof NewAttributeAddedEvent;
  }

  @Override
  public List<Notification> createNotification(NewAttributeAddedEvent event) {
    List<User> allUsers = userRepository.findAll();

    String title = messageSource.getMessage("notification.attribute.title", null, Locale.KOREAN);
    String content = messageSource.getMessage("notification.attribute.content",
        new Object[]{event.attributeName()}, Locale.KOREAN);

    return allUsers.stream()
        .map(user -> Notification.builder()
            .receiver(user)
            .title(title)
            .content(content)
            .level(NotificationLevel.INFO)
            .build())
        .toList();
  }
}
