package com.team3.otboo.domain.notification.service;

import com.team3.otboo.domain.follow.repository.FollowRepository;
import com.team3.otboo.domain.notification.dto.NotificationDto;
import com.team3.otboo.domain.notification.entity.Notification;
import com.team3.otboo.domain.notification.mapper.NotificationMapper;
import com.team3.otboo.domain.notification.repository.NotificationRepository;
import com.team3.otboo.domain.notification.service.strategy.DmReceivedNotificationStrategy;
import com.team3.otboo.domain.notification.service.strategy.FeedLikedNotificationStrategy;
import com.team3.otboo.domain.notification.service.strategy.FollowedUserPostedFeedNotificationStrategy;
import com.team3.otboo.domain.notification.service.strategy.FollowerNotificationStrategy;
import com.team3.otboo.domain.notification.service.strategy.NewCommentNotificationStrategy;
import com.team3.otboo.domain.notification.service.strategy.NotificationStrategy;
import com.team3.otboo.domain.user.entity.User;
import com.team3.otboo.domain.user.repository.UserRepository;
import com.team3.otboo.event.NewFollowerEvent;
import com.team3.otboo.fixture.UserFixture;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

  private NotificationService notificationService;

  // --- Mock 객체들 ---
  @Mock
  private NotificationRepository notificationRepository;
  @Mock
  private NotificationMapper notificationMapper;
  @Mock
  private RedisTemplate<String, Object> redisTemplate;
  @Mock
  private MessageSource messageSource;
  @Mock
  private UserRepository userRepository;
  @Mock
  private FollowRepository followRepository;

  @BeforeEach
  void setUp() {
    List<NotificationStrategy<?>> strategies = List.of(
        new FollowerNotificationStrategy(messageSource),
        new FeedLikedNotificationStrategy(messageSource),
        new NewCommentNotificationStrategy(messageSource),
        new FollowedUserPostedFeedNotificationStrategy(followRepository, userRepository, messageSource),
        new DmReceivedNotificationStrategy(messageSource)
    );
    notificationService = new NotificationService(
        notificationRepository,
        redisTemplate,
        notificationMapper,
        strategies
    );
  }

  @Test
  @DisplayName("NewFollowerEvent가 발생하면 FollowerNotificationStrategy가 알림을 생성한다")
  void handleNewFollowerEvent() {
    // given:
    User followee = UserFixture.createDefaultUser();
    NewFollowerEvent event = new NewFollowerEvent(followee, "새로운 팔로워");

    when(messageSource.getMessage(eq("notification.follower.title"), any(), any(Locale.class)))
        .thenReturn("새로운 팔로워");

    when(notificationMapper.toDto(any(Notification.class)))
        .thenReturn(new NotificationDto(null, null, null, null, null, null));

    // when:
    notificationService.handleEvent(event);

    // then:
    ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
    verify(notificationRepository).save(captor.capture());

    assertThat(captor.getValue().getTitle()).isEqualTo("새로운 팔로워");
    verify(redisTemplate).convertAndSend(eq("notification-channel"), any(NotificationDto.class));
  }
}
