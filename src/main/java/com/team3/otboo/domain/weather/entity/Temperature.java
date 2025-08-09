package com.team3.otboo.domain.weather.entity;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Temperature {
    private Double temperatureCurrent;
    private Double temperatureComparedToDayBefore;
    private Double min;
    private Double max;


    public Temperature(Double currentTemperature, Double previousTemperature, Double min, Double max) {
        this.temperatureCurrent = currentTemperature;
        this.temperatureComparedToDayBefore = temperatureDifference(currentTemperature, previousTemperature);
        this.min = min;
        this.max = max;
    }

    private Double temperatureDifference(Double currentTemperature, Double previousTemperature) {
        return currentTemperature - previousTemperature;
    }

}
