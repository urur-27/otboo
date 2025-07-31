package com.team3.otboo.domain.clothing.mapper;

import com.team3.otboo.domain.clothing.dto.ClothingAttributeDefDto;
import com.team3.otboo.domain.clothing.dto.request.ClothingAttributeDefCreateRequest;
import com.team3.otboo.domain.clothing.entity.Attribute;
import com.team3.otboo.domain.clothing.entity.AttributeOption;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring")
public interface ClothingAttributeDefMapper {

    // request → entity
    @Mapping(target = "options", source = "selectableValues", qualifiedByName = "toOptions")
    Attribute toEntity(ClothingAttributeDefCreateRequest request);

    // entity → dto
    @Mapping(target = "selectableValues", source = "options", qualifiedByName = "toValueList")
    ClothingAttributeDefDto toDto(Attribute attribute);

    List<ClothingAttributeDefDto> toDtoList(List<Attribute> attributes);

    // 커스텀 매핑 메서드
    @Named("toOptions")
    default List<AttributeOption> toOptions(List<String> values) {
        // AttributeOption.of(value, attribute) 형태일 때는 attribute 정보가 없어 단순 생성만 가능
        // 실제 연관관계 연결은 Service에서 보완필요
        return values.stream()
                .map(value -> AttributeOption.of(value, null))
                .toList();
    }

    @Named("toValueList")
    default List<String> toValueList(List<AttributeOption> options) {
        return options.stream()
                .map(AttributeOption::getValue)
                .toList();
    }
}