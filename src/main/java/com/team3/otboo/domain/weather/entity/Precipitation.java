package com.team3.otboo.domain.weather.entity;

import com.team3.otboo.domain.weather.enums.PrecipitationType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Embeddable
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Precipitation {
    @Enumerated(EnumType.STRING)
    private PrecipitationType type;
    private Double amount;
    private Double probability;

    public Precipitation(PrecipitationType type, Double amount, Double probability) {
        this.type = type;
        this.amount = amount;
        this.probability = probability;
    }
}
