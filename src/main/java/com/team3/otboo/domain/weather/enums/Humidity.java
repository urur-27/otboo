package com.team3.otboo.domain.weather.enums;

import jakarta.persistence.Embeddable;

@Embeddable
public class Humidity {
    private Double humidityCurrent;
    private Double humidityComparedToDayBefore;
}
