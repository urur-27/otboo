package com.team3.otboo.domain.weather.enums;

import jakarta.persistence.Embeddable;

@Embeddable
public class Temperature {
    private Double temperatureCurrent;
    private Double temperatureComparedToDayBefore;
    private Double min;
    private Double max;
}
