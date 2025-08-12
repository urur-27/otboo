package com.team3.otboo.domain.weather.dto;

import com.team3.otboo.domain.weather.entity.WindSpeed;
import com.team3.otboo.domain.weather.enums.WindSpeedLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WindSpeedDto {
    private Double speed;
    private WindSpeedLevel asWord;

    @Builder
    private WindSpeedDto(Double speed, WindSpeedLevel asWord) {
        this.speed = speed;
        this.asWord = asWord;
    }

    public static WindSpeedDto create(WindSpeed windSpeed) {
        return WindSpeedDto.builder()
                .speed(windSpeed.getSpeed())
                .asWord(windSpeed.getAsWord())
                .build();
    }
}
