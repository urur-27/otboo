package com.team3.otboo.domain.weather.entity;

import com.team3.otboo.domain.base.entity.BaseEntity;
import com.team3.otboo.domain.user.entity.Location;
import com.team3.otboo.domain.weather.enums.*;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Weather extends BaseEntity {

    private LocalDateTime forecastedAt;

    private LocalDateTime forecastAt;

    @Embedded
    private Location location;

    @Enumerated(EnumType.STRING)
    private SkyStatus skyStatus;

    @Embedded
    private Precipitation precipitation;

    @Embedded
    private Humidity humidity;

    @Embedded
    private Temperature temperature;

    @Embedded
    private WindSpeed windSpeed;

    @Builder
    private Weather(LocalDateTime forecastedAt, LocalDateTime forecastAt, Location location, SkyStatus skyStatus, Precipitation precipitation, Humidity humidity, Temperature temperature, WindSpeed windSpeed) {
        this.forecastedAt = forecastedAt;
        this.forecastAt = forecastAt;
        this.location = location;
        this.skyStatus = skyStatus;
        this.precipitation = precipitation;
        this.humidity = humidity;
        this.temperature = temperature;
        this.windSpeed = windSpeed;
    }
}
