package com.team3.otboo.fixture;

import com.team3.otboo.domain.clothing.entity.Attribute;
import com.team3.otboo.domain.clothing.entity.AttributeOption;
import com.team3.otboo.domain.clothing.entity.Clothing;
import com.team3.otboo.domain.clothing.entity.ClothingAttributeValue;
import com.team3.otboo.domain.user.entity.User;
import java.util.List;

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
}
