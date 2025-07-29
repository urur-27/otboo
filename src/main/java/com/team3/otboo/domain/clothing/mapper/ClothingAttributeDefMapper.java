package com.team3.otboo.domain.clothing.mapper;

import com.team3.otboo.domain.clothing.dto.ClothingAttributeDefDto;
import com.team3.otboo.domain.clothing.dto.request.ClothingAttributeDefCreateRequest;
import com.team3.otboo.domain.clothing.entity.Attribute;
import com.team3.otboo.domain.clothing.entity.AttributeOption;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ClothingAttributeDefMapper {

    public Attribute toEntity(ClothingAttributeDefCreateRequest request) {
        return Attribute.of(request.name(), request.selectableValues());
    }

    public ClothingAttributeDefDto toDto(Attribute attribute) {
        List<String> values = attribute.getOptions().stream()
                .map(AttributeOption::getValue)
                .toList();

        return new ClothingAttributeDefDto(
                attribute.getId(),
                attribute.getName(),
                values
        );
    }
}
