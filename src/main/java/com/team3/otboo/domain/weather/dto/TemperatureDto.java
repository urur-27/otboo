package com.team3.otboo.domain.weather.dto;

import com.team3.otboo.domain.weather.entity.Temperature;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TemperatureDto {
    private Double current;
    private Double comparedToDayBefore;
    private Double min;
    private Double max;

    @Builder
    private TemperatureDto(Double current, Double comparedToDayBefore, Double min, Double max) {
        this.current = current;
        this.comparedToDayBefore = comparedToDayBefore;
        this.min = min;
        this.max = max;
    }

    public static TemperatureDto create(Temperature temperature) {
        return TemperatureDto.builder()
                .current(temperature.getTemperatureCurrent())
                .comparedToDayBefore(temperature.getTemperatureComparedToDayBefore())
                .max(temperature.getMax())
                .min(temperature.getMin())
                .build();
    }
}
