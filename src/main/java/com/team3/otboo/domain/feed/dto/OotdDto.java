package com.team3.otboo.domain.feed.dto;

import com.team3.otboo.domain.clothing.dto.ClothingAttributeWithDefDto;
import java.util.List;
import java.util.UUID;
import lombok.Data;

public record OotdDto(
	UUID clothesId,
	String name,
	String imageUrl,
	String type,
	List<ClothingAttributeWithDefDto> attributes
) {

}
