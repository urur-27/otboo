package com.team3.otboo.domain.clothing.dto;

import java.util.List;
import java.util.UUID;

public record ClothingDto(
        UUID id,
        UUID ownerId,
        String name,
        String imageUrl,
        String type,
        List<ClothingAttributeWithDefDto> attributes
) {}
