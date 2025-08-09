package com.team3.otboo.global.exception.weather;

import com.team3.otboo.global.exception.BusinessException;
import com.team3.otboo.global.exception.ErrorCode;

public class WeatherApiException extends BusinessException {
    public WeatherApiException() {
        super(ErrorCode.WEATHER_NOT_FOUND, ErrorCode.WEATHER_NOT_FOUND.getMessage());
    }

    public WeatherApiException(String detailMessage) {
        super(ErrorCode.WEATHER_NOT_FOUND, detailMessage);
    }
}
