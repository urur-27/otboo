package com.team3.otboo.domain.weather.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HumidityDto {
    private Double current;
    private Double comparedToDayBefore;
}
