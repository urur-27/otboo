package com.team3.otboo.domain.weather.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WindSpeedDto {
    private Double speed;
    private String asWord;
}
