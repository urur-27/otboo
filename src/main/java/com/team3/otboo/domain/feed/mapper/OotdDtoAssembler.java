package com.team3.otboo.domain.feed.mapper;

import com.team3.otboo.domain.clothing.dto.ClothingAttributeWithDefDto;
import com.team3.otboo.domain.clothing.entity.Attribute;
import com.team3.otboo.domain.clothing.entity.AttributeOption;
import com.team3.otboo.domain.clothing.entity.Clothing;
import com.team3.otboo.domain.clothing.entity.ClothingAttributeValue;
import com.team3.otboo.domain.clothing.mapper.ClothingAttributeWithDefDtoMapper;
import com.team3.otboo.domain.clothing.repository.AttributeOptionRepository;
import com.team3.otboo.domain.clothing.repository.AttributeRepository;
import com.team3.otboo.domain.clothing.repository.ClothingRepository;
import com.team3.otboo.domain.feed.dto.OotdDto;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OotdDtoAssembler {

	private final AttributeRepository attributeRepository;
	private final ClothingRepository clothingRepository;
	private final AttributeOptionRepository attributeOptionRepository;

	private final ClothingAttributeWithDefDtoMapper clothingAttributeWithDefDtoMapper;
	private final OotdMapper ootdMapper;

	public List<OotdDto> assemble(List<Clothing> selectedClothes) {

		return selectedClothes.stream()
			.map(clothing -> ootdMapper.toDto(clothing, getAttributes(clothing.getId())))
			.toList();
	}

	private List<ClothingAttributeWithDefDto> getAttributes(UUID clothesId) {
		// clothesId 로 clothing 찾아서 거기서 attribute 랑 attribute option 을 꺼낸다 .
		Clothing clothing = clothingRepository.findById(clothesId).orElseThrow(
			() -> new EntityNotFoundException("clothes not found. clothesId: " + clothesId)
		);
		List<ClothingAttributeValue> attributeValues = clothing.getAttributeValues();

		return attributeValues
			.stream()
			.map(
				attributeValue -> {
					Attribute attribute = attributeValue.getAttribute();
					AttributeOption option = attributeValue.getOption();

					List<AttributeOption> options = attribute.getOptions();

					List<String> selectedValues = options.stream()
						.map(AttributeOption::getValue)
						.toList();

					return clothingAttributeWithDefDtoMapper.toDto(
						attribute.getId(),
						attribute.getName(),
						selectedValues,
						option.getValue()
					);
				}
			)
			.toList();
	}
}
