package com.team3.otboo.fixture;

import com.team3.otboo.domain.clothing.dto.ClothesAttributeDto;
import com.team3.otboo.domain.clothing.dto.ClothesAttributeWithDefDto;
import com.team3.otboo.domain.clothing.dto.ClothesDto;
import com.team3.otboo.domain.clothing.dto.request.ClothesCreateRequest;
import com.team3.otboo.domain.clothing.dto.request.ClothesUpdateRequest;
import java.util.List;
import java.util.UUID;

public class ClothingDtoFixture {
    // 생성 요청용 dto
    public static ClothesCreateRequest sampleCreateRequest(UUID attrId) {
        return new ClothesCreateRequest(
                UUID.randomUUID(),
                "청바지",
                "BOTTOM",
                List.of(new ClothesAttributeDto(attrId, "청색"))
        );
    }

    // 생성 응답 dto
    public static ClothesDto sampleExpectedDto(UUID clothingId, UUID userId, UUID attrId, String imageUrl) {
        ClothesAttributeWithDefDto attrDto = new ClothesAttributeWithDefDto(
                attrId,
                "색상",
                List.of("청색", "검정색"),
                "청색"
        );
        return new ClothesDto(
                clothingId,
                userId,
                "청바지",
                imageUrl,
                "BOTTOM",
                List.of(attrDto)
        );
    }

    // 수정 요청용 DTO
    public static ClothesUpdateRequest sampleUpdateRequest(UUID attrId) {
        return new ClothesUpdateRequest(
                "셔츠",
                "TOP",
                List.of(new ClothesAttributeDto(attrId, "흰색"))
        );
    }

    // 수정 후 기대되는 응답 DTO
    public static ClothesDto sampleUpdatedDto(UUID clothingId, UUID userId, UUID attrId, String imageUrl) {
        ClothesAttributeWithDefDto attrDto = new ClothesAttributeWithDefDto(
                attrId,
                "색상",
                List.of("흰색", "검정색"),
                "흰색"
        );
        return new ClothesDto(
                clothingId,
                userId,
                "셔츠",
                imageUrl,
                "TOP",
                List.of(attrDto)
        );
    }
}
