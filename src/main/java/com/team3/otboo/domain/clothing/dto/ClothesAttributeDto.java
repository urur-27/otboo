package com.team3.otboo.domain.clothing.dto;

import java.util.UUID;

public record ClothesAttributeDto(
        UUID definitionId,
        String value
) {}
