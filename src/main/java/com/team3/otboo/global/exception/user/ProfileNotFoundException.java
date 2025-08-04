package com.team3.otboo.global.exception.user;


import com.team3.otboo.global.exception.BusinessException;
import com.team3.otboo.global.exception.ErrorCode;

public class ProfileNotFoundException extends BusinessException {

  public ProfileNotFoundException() {
    super(ErrorCode.PROFILE_NOT_FOUND, ErrorCode.PROFILE_NOT_FOUND.getMessage());
  }

  public ProfileNotFoundException(String detailMessage) {
    super(ErrorCode.PROFILE_NOT_FOUND, detailMessage);
  }

}
