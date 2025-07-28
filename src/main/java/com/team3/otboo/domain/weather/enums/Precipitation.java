package com.team3.otboo.domain.weather.enums;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public class Precipitation {
    @Enumerated(EnumType.STRING)
    private PrecipitationType type;
    private Double amount;
    private Double probability;
}
