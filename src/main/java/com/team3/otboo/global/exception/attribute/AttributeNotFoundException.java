package com.team3.otboo.global.exception.attribute;

import com.team3.otboo.global.exception.BusinessException;
import com.team3.otboo.global.exception.ErrorCode;

public class AttributeNotFoundException extends BusinessException {

    public AttributeNotFoundException() {
        super(ErrorCode.ATTRIBUTE_NOT_FOUND, ErrorCode.ATTRIBUTE_NOT_FOUND.getMessage());
    }

    public AttributeNotFoundException(String detailMessage) {
        super(ErrorCode.ATTRIBUTE_NOT_FOUND, detailMessage);
    }

}