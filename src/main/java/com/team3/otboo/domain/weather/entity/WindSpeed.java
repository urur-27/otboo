package com.team3.otboo.domain.weather.entity;

import com.team3.otboo.domain.weather.enums.WindSpeedLevel;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WindSpeed {
    private Double speed;

    @Enumerated(EnumType.STRING)
    private WindSpeedLevel asWord;

    public WindSpeed(Double speed, WindSpeedLevel asWord) {
        this.speed = speed;
        this.asWord = asWord;
    }
}
