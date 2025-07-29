package com.team3.otboo.domain.feed.dto;

import java.util.UUID;
import lombok.Data;

public record OotdDto(
	UUID clothesId,
	String name,
	String imageUrl,
	String type,
	List<ClothesAttributeWithDefDto> attributes
) {

}
