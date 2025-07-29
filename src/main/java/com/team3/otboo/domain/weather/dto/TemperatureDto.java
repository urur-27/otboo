package com.team3.otboo.domain.weather.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TemperatureDto {
    private Double current;
    private Double comparedToDayBefore;
    private Double min;
    private Double max;
}
