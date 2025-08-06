package com.team3.otboo.domain.notification.mapper;

import com.team3.otboo.domain.notification.dto.NotificationDto;
import com.team3.otboo.domain.notification.entity.Notification;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

  @Mapping(source = "receiver.id", target = "receiverId")
  NotificationDto toDto(Notification notification);

  List<NotificationDto> toDtoList(List<Notification> notifications);
}
