package com.team3.otboo.domain.clothing.dto.request;

import com.team3.otboo.domain.clothing.dto.ClothingAttributeDto;
import java.util.List;

public record ClothingUpdateRequest(
        String name,
        String type,
        List<ClothingAttributeDto> attributes
) {}
