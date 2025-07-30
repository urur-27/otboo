package com.team3.otboo.domain.weather.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class WeatherDto {
    private UUID id;
    private LocalDateTime forecastedAt;
    private LocalDateTime forecastAt;
    private WeatherAPILocation location;
    private String skyStatus;
    private PrecipitationDto precipitation;
    private HumidityDto humidity;
    private TemperatureDto temperature;
    private WindSpeedDto windSpeed;
}
