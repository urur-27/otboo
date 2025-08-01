package com.team3.otboo.global.exception.attribute;

import com.team3.otboo.global.exception.BusinessException;
import com.team3.otboo.global.exception.ErrorCode;

public class AttributeNameDuplicatedException extends BusinessException {

    public AttributeNameDuplicatedException() {
        super(ErrorCode.ATTRIBUTE_NAME_DUPLICATED, ErrorCode.ATTRIBUTE_NAME_DUPLICATED.getMessage());
    }

    public AttributeNameDuplicatedException(String detailMessage) {
        super(ErrorCode.ATTRIBUTE_NAME_DUPLICATED, detailMessage);
    }

}