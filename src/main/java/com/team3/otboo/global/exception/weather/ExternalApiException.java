package com.team3.otboo.global.exception.weather;

import com.team3.otboo.global.exception.BusinessException;
import com.team3.otboo.global.exception.ErrorCode;

public class ExternalApiException extends BusinessException {
    public ExternalApiException() {
        super(ErrorCode.EXTERNAL_API_FAILED, ErrorCode.EXTERNAL_API_FAILED.getMessage());
    }

    public ExternalApiException(String detailMessage) {
        super(ErrorCode.EXTERNAL_API_FAILED, detailMessage);
    }
}
