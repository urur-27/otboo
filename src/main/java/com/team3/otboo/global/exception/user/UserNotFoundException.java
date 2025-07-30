package com.team3.otboo.global.exception.user;


import com.team3.otboo.global.exception.BusinessException;
import com.team3.otboo.global.exception.ErrorCode;

public class UserNotFoundException extends BusinessException {

  public UserNotFoundException() {
    super(ErrorCode.USER_NOT_FOUND, ErrorCode.USER_NOT_FOUND.getMessage());
  }

  public UserNotFoundException(String detailMessage) {
    super(ErrorCode.USER_NOT_FOUND, detailMessage);
  }

}
