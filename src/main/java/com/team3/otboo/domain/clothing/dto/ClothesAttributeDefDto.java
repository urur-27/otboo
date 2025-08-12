package com.team3.otboo.domain.clothing.dto;

import java.util.List;
import java.util.UUID;

public record ClothesAttributeDefDto(
        UUID id,
        String name,
        List<String> selectableValues
) {}
