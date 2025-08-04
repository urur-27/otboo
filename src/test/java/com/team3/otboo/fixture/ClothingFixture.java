package com.team3.otboo.fixture;

import com.team3.otboo.domain.clothing.dto.ClothingAttributeDto;
import com.team3.otboo.domain.clothing.dto.ClothingAttributeWithDefDto;
import com.team3.otboo.domain.clothing.dto.ClothingDto;
import com.team3.otboo.domain.clothing.dto.request.ClothingUpdateRequest;
import com.team3.otboo.domain.clothing.entity.Attribute;
import com.team3.otboo.domain.clothing.entity.AttributeOption;
import com.team3.otboo.domain.clothing.entity.Clothing;
import com.team3.otboo.domain.clothing.entity.ClothingAttributeValue;
import com.team3.otboo.domain.clothing.dto.request.ClothingCreateRequest;
import com.team3.otboo.domain.user.entity.User;
import java.util.List;
import java.util.UUID;

public class ClothingFixture {

  public static Clothing createTshirt(User owner) {
    Attribute typeAttr = Attribute.of("종류", List.of("반팔티"));
    AttributeOption option = typeAttr.getOptions().getFirst();
    Clothing clothing = Clothing.of("검은색 반팔티", owner);
    ClothingAttributeValue.of(clothing, typeAttr, option);
    return clothing;
  }

  public static Clothing createShorts(User owner) {
    Attribute typeAttr = Attribute.of("종류", List.of("반바지"));
    AttributeOption option = typeAttr.getOptions().getFirst();
    Clothing clothing = Clothing.of("청반바지", owner);
    ClothingAttributeValue.of(clothing, typeAttr, option);
    return clothing;
  }

  public static Clothing createKnit(User owner) {
    Attribute typeAttr = Attribute.of("종류", List.of("긴팔니트"));
    AttributeOption option = typeAttr.getOptions().getFirst();
    Clothing clothing = Clothing.of("겨울니트", owner);
    ClothingAttributeValue.of(clothing, typeAttr, option);
    return clothing;
  }

  public static Clothing createSweatshirt(User owner) {
    Attribute typeAttr = Attribute.of("종류", List.of("맨투맨"));
    AttributeOption option = typeAttr.getOptions().getFirst();
    Clothing clothing = Clothing.of("곰돌이 맨투맨", owner);
    ClothingAttributeValue.of(clothing, typeAttr, option);
    return clothing;
  }

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
