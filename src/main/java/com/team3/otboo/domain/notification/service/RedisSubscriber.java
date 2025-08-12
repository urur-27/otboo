package com.team3.otboo.domain.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team3.otboo.domain.notification.dto.NotificationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener {

  private final ObjectMapper objectMapper;
  private final SseService sseService;

  @Override
  public void onMessage(@NotNull Message message, byte[] pattern) {
    try {
      String publishMessage = new String(message.getBody());
      NotificationDto notificationDto = objectMapper.readValue(publishMessage, NotificationDto.class);

      log.debug("Redis-Sub | Received notification for user: {}", notificationDto.receiverId());

      sseService.sendNotification(notificationDto.receiverId(), notificationDto);
    } catch (Exception e) {
      log.error("Error processing message from Redis", e);
    }
  }
}
