package com.team3.otboo.fixture;

import com.team3.otboo.domain.clothing.dto.ClothingAttributeDto;
import com.team3.otboo.domain.clothing.dto.ClothingAttributeWithDefDto;
import com.team3.otboo.domain.clothing.dto.ClothingDto;
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

  public static ClothingCreateRequest sampleCreateRequest(UUID attrId) {
    return new ClothingCreateRequest(
            UUID.randomUUID(),
            "청바지",
            "BOTTOM",
            List.of(new ClothingAttributeDto(attrId, "청색"))
    );
  }

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
}
