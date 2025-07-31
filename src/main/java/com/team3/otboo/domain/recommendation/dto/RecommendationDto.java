package com.team3.otboo.domain.recommendation.dto;

import com.team3.otboo.domain.clothing.entity.Clothing;
import com.team3.otboo.domain.feed.dto.OotdDto;
import java.util.List;
import java.util.UUID;

public record RecommendationDto(
    UUID weatherId,
    UUID userId,
    List<OotdDto> clothes
) {

}
