package com.team3.otboo.domain.notification.controller;

import com.team3.otboo.domain.notification.service.SseService;
import com.team3.otboo.domain.user.user_details.CustomUserDetails;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SseController {

  private final SseService sseService;

  @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter subscribe(@AuthenticationPrincipal CustomUserDetails userPrincipal) {
    UUID userId = userPrincipal.getId();

    return sseService.subscribe(userId);
  }
}
