package com.team3.otboo.domain.notification.service;

import com.team3.otboo.domain.notification.dto.NotificationSearchCondition;
import com.team3.otboo.domain.notification.enums.SortDirection;
import com.team3.otboo.domain.notification.mapper.NotificationMapper;
import com.team3.otboo.domain.notification.repository.NotificationRepository;
import com.team3.otboo.domain.user.entity.User;
import com.team3.otboo.domain.user.repository.UserRepository;
import com.team3.otboo.fixture.UserFixture;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

  @InjectMocks
  private NotificationService notificationService;

  @Mock
  private UserRepository userRepository;
  @Mock
  private NotificationRepository notificationRepository;
  @Mock
  private NotificationMapper notificationMapper;

  @Test
  @DisplayName("findNotificationsByUserId는 허용되지 않은 sortBy 값에 대해 예외를 발생시킨다")
  void findNotifications_ThrowsException_forInvalidSortBy() {
    // given:
    UUID userId = UUID.randomUUID();
    NotificationSearchCondition invalidCondition = new NotificationSearchCondition(
        null, null, 10, "content", SortDirection.DESC
    );

    // then:
    assertThatThrownBy(() -> notificationService.findNotificationsByUserId(userId, invalidCondition))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Invalid sort property: content");
  }

  @Test
  @DisplayName("findNotificationsByUserId는 올바른 조건으로 Repository를 호출한다")
  void findNotifications_CallsRepository_withValidCondition() {
    // given:
    User user = UserFixture.createDefaultUser();
    UUID userId = user.getId();
    NotificationSearchCondition validCondition = new NotificationSearchCondition(
        null, null, 10, "createdAt", SortDirection.DESC
    );

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(notificationRepository.findByReceiverWithCursor(user, validCondition))
        .thenReturn(List.of());

    // when:
    notificationService.findNotificationsByUserId(userId, validCondition);

    // then:
    verify(notificationRepository).findByReceiverWithCursor(user, validCondition);
  }
}
