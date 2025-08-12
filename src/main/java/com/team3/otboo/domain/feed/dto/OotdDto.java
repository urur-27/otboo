package com.team3.otboo.domain.feed.dto;

import com.team3.otboo.domain.clothing.dto.ClothesAttributeWithDefDto;
import java.util.List;
import java.util.UUID;

// OotdDto -> 의류 아이템의 상세 정보 즉 개별 의류 하나를 나타냄 (outfit of the day 라는 뜻과 맞지 않는다.)
public record OotdDto(
	UUID clothesId,
	String name,
	String imageUrl,
	String type,
	List<ClothesAttributeWithDefDto> attributes
) {

}
