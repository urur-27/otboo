package com.team3.otboo.domain.notification.controller;

import com.team3.otboo.domain.notification.dto.NotificationDto;
import com.team3.otboo.domain.notification.dto.NotificationDtoCursorResponse;
import com.team3.otboo.domain.notification.dto.NotificationSearchCondition;
import com.team3.otboo.domain.notification.enums.SortDirection;
import com.team3.otboo.domain.notification.service.NotificationService;
import com.team3.otboo.domain.user.dto.UserDto;
import com.team3.otboo.domain.user.entity.User;
import com.team3.otboo.domain.user.service.CustomUserDetailsService.CustomUserDetails;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

  private final NotificationService notificationService;

  @GetMapping
  public ResponseEntity<NotificationDtoCursorResponse> getNotifications(
      @AuthenticationPrincipal CustomUserDetails userPrincipal,
      @ModelAttribute NotificationSearchCondition condition
  ) {
    UUID userId = userPrincipal.getId();
    NotificationDtoCursorResponse response = notificationService.findNotificationsByUserId(userId, condition);
    return ResponseEntity.ok(response);
  }
}
