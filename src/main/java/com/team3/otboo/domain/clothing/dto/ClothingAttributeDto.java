package com.team3.otboo.domain.clothing.dto;

import java.util.UUID;

public record ClothingAttributeDto(
        UUID definitionId,
        String value
) {}
