package com.team3.otboo.domain.clothing.dto.request;

import com.team3.otboo.domain.clothing.dto.ClothingAttributeDto;
import java.util.List;
import java.util.UUID;

public record ClothingCreateRequest(
        UUID ownerId,
        String name,
        String type,
        List<ClothingAttributeDto> attributes
) {}
