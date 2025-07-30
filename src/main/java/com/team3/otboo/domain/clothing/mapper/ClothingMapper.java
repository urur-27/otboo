package com.team3.otboo.domain.clothing.mapper;

import com.team3.otboo.domain.clothing.dto.ClothingAttributeWithDefDto;
import com.team3.otboo.domain.clothing.entity.AttributeOption;
import com.team3.otboo.domain.clothing.entity.ClothesType;
import com.team3.otboo.domain.clothing.entity.Clothing;
import com.team3.otboo.domain.clothing.entity.ClothingAttributeValue;
import com.team3.otboo.domain.feed.dto.OotdDto;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface ClothingMapper {

  @Mapping(source = "id", target = "clothesId")
  @Mapping(source = "name", target = "name")
  @Mapping(source = "imageUrl", target = "imageUrl")
  @Mapping(source = "attributeValues", target = "type", qualifiedByName = "extractType")
  @Mapping(source = "attributeValues", target = "attributes", qualifiedByName = "toAttributeDto")
  OotdDto toOotdDto(Clothing clothing);

  List<OotdDto> toOotdDtoList(List<Clothing> clothes);

  @Named("extractType")
  default ClothesType extractType(List<ClothingAttributeValue> values) {
    String typeValue = values.stream()
        .filter(value -> value.getAttribute().getName().equals("종류"))
        .map(value -> value.getOption().getValue())
        .findFirst()
        .orElse(null);

    if (typeValue == null) return ClothesType.ETC;
    try {
      return ClothesType.valueOf(typeValue.toUpperCase());
    } catch (IllegalArgumentException e) {
      return ClothesType.ETC;
    }
  }

  @Named("toAttributeDto")
  default List<ClothingAttributeWithDefDto> toAttributeDto(List<ClothingAttributeValue> values) {
    return values.stream()
        .map(value -> new ClothingAttributeWithDefDto(
            value.getAttribute().getId(),
            value.getAttribute().getName(),
            value.getAttribute().getOptions().stream().map(AttributeOption::getValue)
                .toList(),
            value.getOption().getValue()
        ))
        .toList();
  }
}
