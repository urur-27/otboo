package com.team3.otboo.fixture;

import com.team3.otboo.domain.clothing.dto.ClothingAttributeDto;
import com.team3.otboo.domain.clothing.dto.ClothingAttributeWithDefDto;
import com.team3.otboo.domain.clothing.dto.ClothingDto;
import com.team3.otboo.domain.clothing.dto.request.ClothingCreateRequest;
import com.team3.otboo.domain.clothing.dto.request.ClothingUpdateRequest;
import java.util.List;
import java.util.UUID;

public class ClothingDtoFixture {
    // 생성 요청용 dto
    public static ClothingCreateRequest sampleCreateRequest(UUID attrId) {
        return new ClothingCreateRequest(
                UUID.randomUUID(),
                "청바지",
                "BOTTOM",
                List.of(new ClothingAttributeDto(attrId, "청색"))
        );
    }

    // 생성 응답 dto
    public static ClothingDto sampleExpectedDto(UUID clothingId, UUID userId, UUID attrId, String imageUrl) {
        ClothingAttributeWithDefDto attrDto = new ClothingAttributeWithDefDto(
                attrId,
                "색상",
                List.of("청색", "검정색"),
                "청색"
        );
        return new ClothingDto(
                clothingId,
                userId,
                "청바지",
                imageUrl,
                "BOTTOM",
                List.of(attrDto)
        );
    }

    // 수정 요청용 DTO
    public static ClothingUpdateRequest sampleUpdateRequest(UUID attrId) {
        return new ClothingUpdateRequest(
                "셔츠",
                "TOP",
                List.of(new ClothingAttributeDto(attrId, "흰색"))
        );
    }

    // 수정 후 기대되는 응답 DTO
    public static ClothingDto sampleUpdatedDto(UUID clothingId, UUID userId, UUID attrId, String imageUrl) {
        ClothingAttributeWithDefDto attrDto = new ClothingAttributeWithDefDto(
                attrId,
                "색상",
                List.of("흰색", "검정색"),
                "흰색"
        );
        return new ClothingDto(
                clothingId,
                userId,
                "셔츠",
                imageUrl,
                "TOP",
                List.of(attrDto)
        );
    }
}
