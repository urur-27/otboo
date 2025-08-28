package com.team3.otboo.domain.notification.controller;

import com.team3.otboo.domain.notification.dto.NotificationDtoCursorResponse;
import com.team3.otboo.domain.notification.dto.NotificationSearchCondition;
import com.team3.otboo.domain.notification.service.NotificationService;
import com.team3.otboo.domain.user.user_details.CustomUserDetails;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
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

  @DeleteMapping("/{notificationId}")
  public ResponseEntity<Void> readNotification(@PathVariable UUID notificationId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    notificationService.readNotification(notificationId, userDetails.getId());

    return ResponseEntity.noContent().build();
  }
}
