package com.team3.otboo.global.exception.user;

import com.team3.otboo.global.exception.BusinessException;
import com.team3.otboo.global.exception.ErrorCode;

public class RoleNotFoundException extends BusinessException {
    public RoleNotFoundException() {
        super(ErrorCode.ROLE_NOT_FOUND, ErrorCode.ROLE_NOT_FOUND.getMessage());
    }

    public RoleNotFoundException(String detailMessage) {
        super(ErrorCode.ROLE_NOT_FOUND, detailMessage);
    }
}
