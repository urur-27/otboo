package com.team3.otboo.global.exception.clothing;

import com.team3.otboo.global.exception.BusinessException;
import com.team3.otboo.global.exception.ErrorCode;

public class ClothingNotFoundException extends BusinessException {

    public ClothingNotFoundException() {
        super(ErrorCode.CLOTHING_NOT_FOUND, ErrorCode.CLOTHING_NOT_FOUND.getMessage());
    }

    public ClothingNotFoundException(String detailMessage) {
        super(ErrorCode.CLOTHING_NOT_FOUND, detailMessage);
    }

}