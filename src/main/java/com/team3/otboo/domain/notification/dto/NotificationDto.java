package com.team3.otboo.domain.notification.dto;

import com.team3.otboo.domain.notification.entity.NotificationLevel;
import java.time.Instant;
import java.util.UUID;

public record NotificationDto(
    UUID id,
    Instant createdAt,
    UUID receiverId,
    String title,
    String content,
    NotificationLevel level
) {

}
