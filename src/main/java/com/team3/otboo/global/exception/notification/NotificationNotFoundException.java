package com.team3.otboo.global.exception.notification;

import com.team3.otboo.global.exception.BusinessException;
import com.team3.otboo.global.exception.ErrorCode;

public class NotificationNotFoundException extends BusinessException {

  public NotificationNotFoundException() {
    super(ErrorCode.NOTIFICATION_NOT_FOUND, ErrorCode.NOTIFICATION_NOT_FOUND.getMessage());
  }

  public NotificationNotFoundException(String detailMessage) {
    super(ErrorCode.NOTIFICATION_NOT_FOUND, detailMessage);
  }
}
