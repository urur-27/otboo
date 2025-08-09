package com.team3.otboo.domain.weather.dto;

import java.util.UUID;

import com.team3.otboo.domain.weather.entity.*;
import com.team3.otboo.domain.weather.enums.SkyStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class WeatherDto {
    private UUID id;
    private LocalDateTime forecastedAt;
    private LocalDateTime forecastAt;
    private WeatherLocation location;
    private SkyStatus skyStatus;
    private Precipitation precipitation;
    private Humidity humidity;
    private Temperature temperature;
    private WindSpeed windSpeed;

    @Builder
    private WeatherDto(UUID id, LocalDateTime forecastedAt, LocalDateTime forecastAt, WeatherLocation location, SkyStatus skyStatus, Precipitation precipitation, Humidity humidity, Temperature temperature, WindSpeed windSpeed) {
        this.id = id;
        this.forecastedAt = forecastedAt;
        this.forecastAt = forecastAt;
        this.location = location;
        this.skyStatus = skyStatus;
        this.precipitation = precipitation;
        this.humidity = humidity;
        this.temperature = temperature;
        this.windSpeed = windSpeed;
    }

    public static WeatherDto from(Weather weather) {
        return WeatherDto.builder()
                .id(weather.getId())
                .forecastedAt(weather.getForecastedAt())
                .forecastAt(weather.getForecastAt())
                .location(weather.getLocation())
                .skyStatus(weather.getSkyStatus())
                .precipitation(weather.getPrecipitation())
                .humidity(weather.getHumidity())
                .temperature(weather.getTemperature())
                .windSpeed(weather.getWindSpeed())
                .build();
    }
}
