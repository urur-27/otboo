package com.team3.otboo.domain.clothing.dto.response;

import java.util.List;
import java.util.Map;

public record ClothingLlmResponse(
        String name,
        String type,
        List<Map<String, String>> attributes
) {}