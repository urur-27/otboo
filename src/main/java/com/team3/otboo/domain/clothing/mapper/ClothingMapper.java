package com.team3.otboo.domain.clothing.mapper;

import com.team3.otboo.domain.clothing.dto.ClothingAttributeWithDefDto;
import com.team3.otboo.domain.clothing.dto.ClothingDto;
import com.team3.otboo.domain.clothing.dto.request.ClothingCreateRequest;
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

  @Mapping(source = "owner.id", target = "ownerId")
  @Mapping(source = "attributeValues", target = "attributes", qualifiedByName = "toAttributeDto")
  ClothingDto toDto(Clothing clothing);

  @Mapping(target = "id", ignore = true) // PK 자동 생성
  @Mapping(target = "owner", ignore = true) // 서비스에서 세팅
  @Mapping(target = "imageUrl", ignore = true) // 서비스에서 세팅
  @Mapping(target = "purchaseUrl", ignore = true) // (옵션) 추후 추가
  @Mapping(target = "attributeValues", ignore = true) // 속성값 매핑은 별도 처리(서비스 계층)
  Clothing toEntity(ClothingCreateRequest request);
}
