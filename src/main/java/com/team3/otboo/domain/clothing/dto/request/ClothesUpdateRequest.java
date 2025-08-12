package com.team3.otboo.domain.clothing.dto.request;

import com.team3.otboo.domain.clothing.dto.ClothesAttributeDto;
import java.util.List;

public record ClothesUpdateRequest(
        String name,
        String type,
        List<ClothesAttributeDto> attributes
) {}
