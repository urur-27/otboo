package com.team3.otboo.domain.recommendation.service.strategy;

import com.team3.otboo.domain.clothing.entity.Clothing;
import com.team3.otboo.domain.clothing.mapper.ClothingMapper;
import com.team3.otboo.domain.feed.dto.OotdDto;
import com.team3.otboo.domain.recommendation.dto.RecommendationDto;
import com.team3.otboo.domain.user.dto.ProfileDto;
import com.team3.otboo.domain.weather.dto.WeatherDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RuleBasedStrategy implements RecommendationStrategy {

  private final ClothingMapper clothingMapper;

  private static final double HOT_WEATHER_THRESHOLD = 25.0;
  private static final String ATTRIBUTE_TYPE = "종류";
  private static final String OPTION_TSHIRT = "반팔티";
  private static final String OPTION_SHORTS = "반바지";

  @Override
  public RecommendationDto recommend(ProfileDto profile, WeatherDto weather, List<Clothing> clothes) {
    List<Clothing> recommendedItems;

    if (weather.getTemperature().getCurrent() >= HOT_WEATHER_THRESHOLD) {
      recommendedItems = findHotWeatherClothes(clothes);
    } else {
      recommendedItems = List.of();
    }

    List<OotdDto> recommendedOotds = clothingMapper.toOotdDtoList(recommendedItems);

    return new RecommendationDto(
        weather.getId(),
        profile.getUserId(),
        recommendedOotds
    );
  }

  private List<Clothing> findHotWeatherClothes(List<Clothing> clothes) {
    return clothes.stream()
        .filter(clothing -> hasAttributeOption(clothing, OPTION_TSHIRT) ||
            hasAttributeOption(clothing, OPTION_SHORTS))
        .toList();
  }

  private boolean hasAttributeOption(Clothing clothing, String optionValue) {
    return clothing.getAttributeValues().stream()
        .anyMatch(value ->
            value.getAttribute().getName().equals(RuleBasedStrategy.ATTRIBUTE_TYPE) &&
                value.getOption().getValue().equals(optionValue)
        );
  }
}
