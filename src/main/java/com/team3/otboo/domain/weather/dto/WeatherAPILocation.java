package com.team3.otboo.domain.weather.dto;


import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class WeatherAPILocation {
    private Double latitude;
    private Double longitude;
    private Integer x;
    private Integer y;
    private List<String> locationNames;
}
