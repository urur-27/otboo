package com.team3.otboo.global.exception.attribute;

import com.team3.otboo.global.exception.BusinessException;
import com.team3.otboo.global.exception.ErrorCode;

public class AttributeOptionEmptyException extends BusinessException {

    public AttributeOptionEmptyException() {
        super(ErrorCode.ATTRIBUTE_OPTION_EMPTY, ErrorCode.ATTRIBUTE_OPTION_EMPTY.getMessage());
    }

    public AttributeOptionEmptyException(String detailMessage) {
        super(ErrorCode.ATTRIBUTE_OPTION_EMPTY, detailMessage);
    }
}
