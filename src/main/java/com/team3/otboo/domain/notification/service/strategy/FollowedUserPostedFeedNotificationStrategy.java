package com.team3.otboo.domain.notification.service.strategy;

import com.team3.otboo.domain.follow.entity.Follow;
import com.team3.otboo.domain.follow.repository.FollowRepository;
import com.team3.otboo.domain.notification.entity.Notification;
import com.team3.otboo.domain.notification.entity.NotificationLevel;
import com.team3.otboo.domain.user.entity.User;
import com.team3.otboo.domain.user.repository.UserRepository;
import com.team3.otboo.event.FollowedUserPostedFeedEvent;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FollowedUserPostedFeedNotificationStrategy implements NotificationStrategy<FollowedUserPostedFeedEvent> {

  private final FollowRepository followRepository;
  private final UserRepository userRepository;
  private final MessageSource messageSource;

  @Override
  public boolean supports(Object event) {
    return event instanceof FollowedUserPostedFeedEvent;
  }

  @Override
  public List<Notification> createNotification(FollowedUserPostedFeedEvent event) {
    List<Follow> follows = followRepository.findAllByFolloweeId(event.author().getId());

    List<UUID> followerIds = follows.stream()
        .map(Follow::getFollowerId)
        .toList();

    if (followerIds.isEmpty()) {
      return List.of();
    }

    List<User> followers = userRepository.findAllById(followerIds);

    String title = messageSource.getMessage("notification.new_feed.title", null, Locale.KOREAN);
    String content = messageSource.getMessage("notification.new_feed.content",
        new Object[]{event.author().getUsername()},
        Locale.KOREAN);

    return followers.stream()
        .map(follower -> Notification.builder()
            .receiver(follower)
            .title(title)
            .content(content)
            .level(NotificationLevel.INFO)
            .build())
        .toList();
  }
}
