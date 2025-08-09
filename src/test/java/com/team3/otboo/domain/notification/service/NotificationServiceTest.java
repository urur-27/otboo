package com.team3.otboo.domain.notification.service;

import com.team3.otboo.domain.follow.entity.Follow;
import com.team3.otboo.domain.follow.repository.FollowRepository;
import com.team3.otboo.domain.notification.dto.NotificationDto;
import com.team3.otboo.domain.notification.entity.Notification;
import com.team3.otboo.domain.notification.mapper.NotificationMapper;
import com.team3.otboo.domain.notification.repository.NotificationRepository;
import com.team3.otboo.domain.notification.service.strategy.DmReceivedNotificationStrategy;
import com.team3.otboo.domain.notification.service.strategy.FeedLikedNotificationStrategy;
import com.team3.otboo.domain.notification.service.strategy.FollowedUserPostedFeedNotificationStrategy;
import com.team3.otboo.domain.notification.service.strategy.FollowerNotificationStrategy;
import com.team3.otboo.domain.notification.service.strategy.NewAttributeNotificationStrategy;
import com.team3.otboo.domain.notification.service.strategy.NewCommentNotificationStrategy;
import com.team3.otboo.domain.notification.service.strategy.NotificationStrategy;
import com.team3.otboo.domain.user.entity.User;
import com.team3.otboo.domain.user.repository.UserRepository;
import com.team3.otboo.event.DmReceivedEvent;
import com.team3.otboo.event.FeedLikedEvent;
import com.team3.otboo.event.FollowedUserPostedFeedEvent;
import com.team3.otboo.event.NewAttributeAddedEvent;
import com.team3.otboo.event.NewCommentEvent;
import com.team3.otboo.event.NewFollowerEvent;
import com.team3.otboo.fixture.UserFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

  private NotificationService notificationService;

  @Mock
  private NotificationRepository notificationRepository;
  @Mock
  private SseService sseService;
  @Mock
  private NotificationMapper notificationMapper;
  @Mock
  private MessageSource messageSource;
  @Mock
  private FollowRepository followRepository;
  @Mock
  private UserRepository userRepository;

  @BeforeEach
  void setUp() {
    List<NotificationStrategy<?>> strategies = List.of(
        new FollowerNotificationStrategy(messageSource),
        new FeedLikedNotificationStrategy(messageSource),
        new NewCommentNotificationStrategy(messageSource),
        new FollowedUserPostedFeedNotificationStrategy(followRepository, userRepository,
            messageSource),
        new NewAttributeNotificationStrategy(userRepository, messageSource),
        new DmReceivedNotificationStrategy(messageSource)
    );

    // 주인공 객체를 직접 생성합니다.
    notificationService = new NotificationService(
        notificationRepository,
        sseService,
        notificationMapper,
        strategies
    );
  }

  @Test
  @DisplayName("NewFollowerEvent가 발생하면 FollowerNotificationStrategy가 알림을 생성한다")
  void handleNewFollowerEvent() {
    // given:
    User followee = UserFixture.createDefaultUser();
    String followerName = "새로운 팔로워";
    NewFollowerEvent event = new NewFollowerEvent(followee, followerName);

    String expectedTitle = "새로운 팔로워";
    String expectedContent = followerName + "님이 회원님을 팔로우하기 시작했습니다.";
    when(messageSource.getMessage(eq("notification.follower.title"), any(), any(Locale.class)))
        .thenReturn(expectedTitle);
    when(messageSource.getMessage(eq("notification.follower.content"), any(), any(Locale.class)))
        .thenReturn(expectedContent);

    NotificationDto fakeDto = new NotificationDto(UUID.randomUUID(), Instant.now(), followee.getId(), expectedTitle, expectedContent, null);
    when(notificationMapper.toDto(any(Notification.class))).thenReturn(fakeDto);

    // when:
    notificationService.handleEvent(event);

    // then:
    ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
    verify(notificationRepository).save(captor.capture());

    Notification savedNotification = captor.getValue();
    assertThat(savedNotification.getReceiver()).isEqualTo(followee);
    assertThat(savedNotification.getTitle()).isEqualTo(expectedTitle);
    assertThat(savedNotification.getContent()).isEqualTo(expectedContent);

    verify(sseService).sendNotification(eq(followee.getId()), eq(fakeDto));
  }

  @Test
  @DisplayName("FeedLikedEvent가 발생하면 FeedLikedNotificationStrategy가 알림을 생성한다")
  void handleFeedLikedEvent() {
    // given:
    User feedOwner = UserFixture.createDefaultUser();
    String likerName = "좋아요요정";
    UUID feedId = UUID.randomUUID();
    FeedLikedEvent event = new FeedLikedEvent(feedOwner, likerName, feedId);

    String expectedTitle = "새로운 좋아요";
    String expectedContent = likerName + "님이 회원님의 피드를 좋아합니다.";
    when(messageSource.getMessage(eq("notification.like.title"), any(), any(Locale.class)))
        .thenReturn(expectedTitle);
    when(messageSource.getMessage(eq("notification.like.content"), any(), any(Locale.class)))
        .thenReturn(expectedContent);

    NotificationDto fakeDto = new NotificationDto(UUID.randomUUID(), Instant.now(), feedOwner.getId(), expectedTitle, expectedContent, null);
    when(notificationMapper.toDto(any(Notification.class))).thenReturn(fakeDto);

    // when:
    notificationService.handleEvent(event);

    // then:
    ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
    verify(notificationRepository).save(captor.capture());

    Notification savedNotification = captor.getValue();
    assertThat(savedNotification.getReceiver()).isEqualTo(feedOwner);
    assertThat(savedNotification.getTitle()).isEqualTo(expectedTitle);
    assertThat(savedNotification.getContent()).isEqualTo(expectedContent);

    verify(sseService).sendNotification(eq(feedOwner.getId()), eq(fakeDto));
  }

  @Test
  @DisplayName("NewCommentEvent가 발생하면 NewCommentNotificationStrategy가 알림을 생성한다")
  void handleNewCommentEvent() {
    // given:
    User feedOwner = UserFixture.createDefaultUser();
    String commenterName = "일침상인";
    NewCommentEvent event = new NewCommentEvent(feedOwner, commenterName, UUID.randomUUID());

    // MessageSource와 Mapper의 행동을 정의
    when(messageSource.getMessage(eq("notification.comment.title"), any(), any(Locale.class)))
        .thenReturn("새로운 댓글");
    when(notificationMapper.toDto(any(Notification.class)))
        .thenReturn(new NotificationDto(null, null, null, null, null, null));

    // when:
    notificationService.handleEvent(event);

    // then:
    ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
    verify(notificationRepository).save(captor.capture());

    Notification savedNotification = captor.getValue();
    assertThat(savedNotification.getReceiver()).isEqualTo(feedOwner);
    assertThat(savedNotification.getTitle()).isEqualTo("새로운 댓글");

    verify(sseService).sendNotification(eq(feedOwner.getId()), any(NotificationDto.class));
  }

  @Test
  @DisplayName("FollowedUserPostedFeedEvent가 발생하면, 작성자를 팔로우하는 모든 사용자에게 알림을 보내야 한다")
  void handleFollowedUserPostedFeedEvent() {
    // given:
    User author = UserFixture.createDefaultUser();
    User follower1 = UserFixture.createAnotherUser();
    User follower2 = UserFixture.createThirdUser();
    FollowedUserPostedFeedEvent event = new FollowedUserPostedFeedEvent(author, UUID.randomUUID());

    List<Follow> follows = List.of(
        Follow.create(author.getId(), follower1.getId()),
        Follow.create(author.getId(), follower2.getId())
    );
    when(followRepository.findAllByFolloweeId(author.getId())).thenReturn(follows);
    when(userRepository.findAllById(anyList())).thenReturn(List.of(follower1, follower2));

    when(notificationMapper.toDto(any(Notification.class)))
        .thenReturn(new NotificationDto(UUID.randomUUID(), null, null, "제목", "내용", null));


    // when:
    notificationService.handleEvent(event);

    // then:
    verify(notificationRepository, times(2)).save(any(Notification.class));
    verify(sseService, times(2)).sendNotification(any(UUID.class), any(NotificationDto.class));
  }


  @Test
  @DisplayName("NewAttributeAddedEvent가 발생하면, 모든 사용자에게 알림을 보내야 한다")
  void handleNewAttributeAddedEvent() {
    // given:
    NewAttributeAddedEvent event = new NewAttributeAddedEvent("재질");

    User user1 = UserFixture.createDefaultUser();
    User user2 = UserFixture.createAnotherUser();
    List<User> allUsers = List.of(user1, user2);

    when(userRepository.findAll()).thenReturn(allUsers);
    when(notificationMapper.toDto(any(Notification.class)))
        .thenReturn(new NotificationDto(UUID.randomUUID(), null, null, "제목", "내용", null));

    // when:
    notificationService.handleEvent(event);

    // then:
    verify(notificationRepository, times(2)).save(any(Notification.class));
    verify(sseService, times(2)).sendNotification(any(UUID.class), any(NotificationDto.class));
  }

  @Test
  @DisplayName("DmReceivedEvent가 발생하면, 수신자에게 알림을 보내야 한다")
  void handleDmReceivedEvent() {
    // given:
    User receiver = UserFixture.createDefaultUser();
    String senderName = "메시지보내는사람";
    DmReceivedEvent event = new DmReceivedEvent(receiver, senderName);

    String expectedTitle = "새로운 쪽지";
    String expectedContent = senderName + "님으로부터 새로운 쪽지가 도착했습니다.";

    when(messageSource.getMessage(eq("notification.dm.title"), any(), any(Locale.class)))
        .thenReturn(expectedTitle);
    when(messageSource.getMessage(eq("notification.dm.content"), any(), any(Locale.class)))
        .thenReturn(expectedContent);

    when(notificationMapper.toDto(any(Notification.class)))
        .thenReturn(new NotificationDto(UUID.randomUUID(), null, receiver.getId(), null, null, null));

    // when:
    notificationService.handleEvent(event);

    // then:
    ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
    verify(notificationRepository).save(captor.capture());

    Notification savedNotification = captor.getValue();
    assertThat(savedNotification).isNotNull();
    assertThat(savedNotification.getReceiver()).isEqualTo(receiver);
    assertThat(savedNotification.getTitle()).isEqualTo(expectedTitle);
    assertThat(savedNotification.getContent()).isEqualTo(expectedContent);

    verify(sseService).sendNotification(eq(receiver.getId()), any(NotificationDto.class));
  }
}
