package com.team3.otboo.domain.weather.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WeatherSummaryDto {
    private String weatherId;
    private String skyStatus;
    private PrecipitationDto precipitation;
    private TemperatureDto temperature;
}
