package com.team3.otboo.domain.clothing.dto.request;

import java.util.List;

public record ClothingAttributeDefCreateRequest(
        String name,
        List<String> selectableValues
) {}
