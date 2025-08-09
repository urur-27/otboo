package com.team3.otboo.domain.weather.entity;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Embeddable
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Humidity {
    private Double humidityCurrent;
    private Double humidityComparedToDayBefore;

    public Humidity(Double humidityCurrent, Double previousHumidity) {
        this.humidityCurrent = humidityCurrent;
        this.humidityComparedToDayBefore = humidityDifference(humidityCurrent, previousHumidity);
    }

    private Double humidityDifference(Double humidityCurrent, Double previousHumidity) {
        return humidityCurrent - previousHumidity;
    }


}
