package com.team3.otboo.domain.recommendation.dto;

import com.team3.otboo.domain.clothing.entity.Clothing;
import java.util.List;

public record RecommendationDto(
    List<Clothing> recommendedItems,
    String comment
) {

}
