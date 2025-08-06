package com.team3.otboo.domain.notification.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.team3.otboo.domain.notification.dto.NotificationDto;
import com.team3.otboo.domain.notification.entity.Notification;
import com.team3.otboo.domain.notification.mapper.NotificationMapper;
import com.team3.otboo.domain.notification.repository.NotificationRepository;
import com.team3.otboo.domain.user.entity.User;
import com.team3.otboo.event.NewFollowerEvent;
import com.team3.otboo.fixture.UserFixture;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

  @InjectMocks
  private NotificationService notificationService;

  @Mock
  private NotificationRepository notificationRepository;
  @Mock
  private SseService sseService;
  @Mock
  private NotificationMapper notificationMapper;

  @Test
  @DisplayName("NewFollowerEvent가 발생하면, 알림을 생성하고 저장한 뒤 SSE로 전송해야 한다")
  void handleNewFollowerEvent() {
    //given:
    User followee = UserFixture.createDefaultUser(); // 팔로우 당한 사람
    String followerName = "새로운 팔로워";
    NewFollowerEvent event = new NewFollowerEvent(followee, followerName);

    NotificationDto fakeDto = new NotificationDto(UUID.randomUUID(), null, followee.getId(), "제목", "내용", null);
    when(notificationMapper.toDto(any(Notification.class))).thenReturn(fakeDto);

    // when: "새로운 팔로워가 생겼다"는 이벤트 발행
    notificationService.handleNewFollowerEvent(event);

    //then: 검증
    verify(notificationRepository).save(any(Notification.class));

    verify(sseService).sendNotification(eq(followee.getId()), any(NotificationDto.class));
  }

}
