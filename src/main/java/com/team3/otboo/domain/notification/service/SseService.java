package com.team3.otboo.domain.notification.service;

import com.team3.otboo.domain.notification.dto.NotificationDto;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
public class SseService {

  private final Map<UUID, SseEmitter> emitterMap = new ConcurrentHashMap<>();

  public SseEmitter subscribe(UUID userId) {
    SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

    emitter.onCompletion(() -> emitterMap.remove(userId));
    emitter.onTimeout(() -> emitterMap.remove(userId));

    emitterMap.put(userId, emitter);

    try {
      emitter.send(SseEmitter.event()
          .id(userId + "_" + System.currentTimeMillis())
          .name("connect")
          .data("SSE Connection successful. userId=" + userId));
    } catch (IOException e) {
      log.error("SSE initial connection error for userId: {}", userId, e);
      emitter.completeWithError(e);
    }
    return emitter;
  }

  public void sendNotification(UUID userId, NotificationDto notificationDto) {
    SseEmitter emitter = emitterMap.get(userId);
    if (emitter != null) {
      try {
        emitter.send(SseEmitter.event()
            .id(notificationDto.id().toString())
            .name("notification")
            .data(notificationDto));
      } catch (IOException e) {
        log.error("SSE notification send error for userId: {}", userId, e);
        emitterMap.remove(userId);
      }
    }
  }
}
