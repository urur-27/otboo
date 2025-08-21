package com.team3.otboo.domain.weather.dto;

import com.team3.otboo.domain.weather.entity.Precipitation;
import com.team3.otboo.domain.weather.enums.PrecipitationType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
public class PrecipitationDto {

	private PrecipitationType type;
	private Double amount;
	private Double probability;

	@Builder
	private PrecipitationDto(PrecipitationType type, Double amount, Double probability) {
		this.type = type;
		this.amount = amount;
		this.probability = probability;
	}

	public static PrecipitationDto create(Precipitation precipitation) {
		return new PrecipitationDto(precipitation.getType(), precipitation.getAmount(),
			precipitation.getProbability());
	}
}
