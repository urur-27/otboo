package com.team3.otboo.domain.weather.entity;

import com.team3.otboo.domain.base.entity.BaseEntity;
import com.team3.otboo.domain.weather.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "weather", uniqueConstraints = {@UniqueConstraint(name = "uk_weather_forecast_grid", columnNames = {"forecast_at", "x", "y"})}
)
public class Weather extends BaseEntity {

    private LocalDateTime forecastedAt;

    private LocalDateTime forecastAt;

    @Embedded
    private WeatherLocation location;

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

//    @Builder
//    private Weather(LocalDateTime forecastedAt, LocalDateTime forecastAt, SkyStatus skyStatus, WeatherLocation location, Precipitation precipitation, Humidity humidity, Temperature temperature, WindSpeed windSpeed) {
//        this.forecastedAt = forecastedAt;
//        this.forecastAt = forecastAt;
//        this.skyStatus = skyStatus;
//        this.location = location;
//        this.precipitation = precipitation;
//        this.humidity = humidity;
//        this.temperature = temperature;
//        this.windSpeed = windSpeed;
//    }

    public static Weather of(LocalDateTime forecastedAt, LocalDateTime forecastAt, SkyStatus skyStatus, WeatherLocation location, Precipitation precipitation, Humidity humidity, Temperature temperature, WindSpeed windSpeed) {
        return Weather
                .builder()
                .forecastedAt(forecastedAt)
                .forecastAt(forecastAt)
                .skyStatus(skyStatus)
                .location(location)
                .precipitation(precipitation)
                .humidity(humidity)
                .temperature(temperature)
                .windSpeed(windSpeed)
                .build();
    }

    public void updateFrom(LocalDateTime newForecastedAt, LocalDateTime newForecastAt, SkyStatus newSkyStatus, WeatherLocation newLocation, Precipitation newPrecipitation, Humidity newHumidity, Temperature newTemperature, WindSpeed newWindSpeed) {
        if (newForecastedAt != null) {
            this.forecastedAt = newForecastedAt;
        }
        if (newForecastAt != null) {
            this.forecastAt = newForecastAt;
        }
        if (newSkyStatus != null) {
            this.skyStatus = newSkyStatus;
        }
        if (newLocation != null) {
            // assumes WeatherLocation has its own updateFrom(WeatherLocation) method
            this.location.updateFrom(newLocation);
        }
        if (newPrecipitation != null) {
            this.precipitation = newPrecipitation;
        }
        if (newHumidity != null) {
            this.humidity = newHumidity;
        }
        if (newTemperature != null) {
            this.temperature = newTemperature;
        }
        if (newWindSpeed != null) {
            this.windSpeed = newWindSpeed;
        }
    }
}
