package com.team3.otboo.domain.weather.enums;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public class WindSpeed {
    private Double speed;

    @Enumerated(EnumType.STRING)
    private WindSpeedLevel asWord;
}
