package com.team3.otboo.domain.clothing.dto.request;

import com.team3.otboo.domain.clothing.dto.ClothesAttributeDto;
import java.util.List;
import java.util.UUID;

public record ClothesCreateRequest(
        UUID ownerId,
        String name,
        String type,
        List<ClothesAttributeDto> attributes
) {}
