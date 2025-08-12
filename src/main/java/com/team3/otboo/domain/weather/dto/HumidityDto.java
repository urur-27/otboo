package com.team3.otboo.domain.weather.dto;

import com.team3.otboo.domain.weather.entity.Humidity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HumidityDto {
    private Double current;
    private Double comparedToDayBefore;

    @Builder
    private HumidityDto(Double current, Double comparedToDayBefore) {
        this.current = current;
        this.comparedToDayBefore = comparedToDayBefore;
    }

    public static HumidityDto create(Humidity humidity) {
        return HumidityDto.builder()
                .current(humidity.getHumidityCurrent())
                .comparedToDayBefore(humidity.getHumidityComparedToDayBefore())
                .build();
    }
}
