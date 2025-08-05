package com.team3.otboo.domain.clothing.mapper;

import com.team3.otboo.domain.clothing.dto.ClothingAttributeWithDefDto;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClothingAttributeWithDefDtoMapper {

	public ClothingAttributeWithDefDto toDto(
		UUID definitionId,
		String definitionName,
		List<String> selectedValues,
		String value
	) {
		return new ClothingAttributeWithDefDto(
			definitionId,
			definitionName,
			selectedValues,
			value
		);
	}
}
