package com.team3.otboo.domain.feed.mapper;

import com.team3.otboo.domain.clothing.dto.ClothesAttributeWithDefDto;
import com.team3.otboo.domain.clothing.entity.Clothing;
import com.team3.otboo.domain.feed.dto.OotdDto;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class OotdMapper {

	public OotdDto toDto(Clothing clothing, List<ClothesAttributeWithDefDto> attributes) {
		return new OotdDto(
			clothing.getId(),
			clothing.getName(),
			clothing.getImageUrl(),
			clothing.getType(),
			attributes
		);
	}
}
