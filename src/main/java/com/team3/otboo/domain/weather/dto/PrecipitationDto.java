package com.team3.otboo.domain.weather.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PrecipitationDto {
    private String type;
    private Double amount;
    private Double probability;
}
