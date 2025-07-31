package com.team3.otboo.global.exception.attributeoption;

import com.team3.otboo.global.exception.BusinessException;
import com.team3.otboo.global.exception.ErrorCode;

public class AttributeOptionNotFoundException extends BusinessException {

    public AttributeOptionNotFoundException() {
        super(ErrorCode.ATTRIBUTEOPTION_NOT_FOUND, ErrorCode.ATTRIBUTEOPTION_NOT_FOUND.getMessage());
    }

    public AttributeOptionNotFoundException(String detailMessage) {
        super(ErrorCode.ATTRIBUTEOPTION_NOT_FOUND, detailMessage);
    }

}
