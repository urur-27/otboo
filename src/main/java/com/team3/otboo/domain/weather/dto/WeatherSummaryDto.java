package com.team3.otboo.domain.weather.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherSummaryDto {

	private String weatherId;
	private String skyStatus;
	private PrecipitationDto precipitation;
	private TemperatureDto temperature;
}
