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
  private static final double COLD_WEATHER_THRESHOLD = 15.0;
  private static final String ATTRIBUTE_TYPE = "종류";
  private static final String OPTION_TSHIRT = "반팔티";
  private static final String OPTION_SHORTS = "반바지";
  private static final String OPTION_KNIT = "긴팔니트";

  @Override
  public RecommendationDto recommend(ProfileDto profile, WeatherDto weather, List<Clothing> clothes) {
    List<Clothing> recommendedItems;
    double currentTemp = weather.getTemperature().getTemperatureCurrent();

    if (currentTemp >= HOT_WEATHER_THRESHOLD) {
      recommendedItems = findHotWeatherClothes(clothes);
    } else if (currentTemp <= COLD_WEATHER_THRESHOLD) {
      recommendedItems = findColdWeatherClothes(clothes);
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
        .filter(clothing -> hasAttributeOption(clothing, ATTRIBUTE_TYPE, OPTION_TSHIRT) ||
            hasAttributeOption(clothing, ATTRIBUTE_TYPE, OPTION_SHORTS))
        .toList();
  }

  private List<Clothing> findColdWeatherClothes(List<Clothing> clothes) {
    return clothes.stream()
        .filter(clothing -> hasAttributeOption(clothing, ATTRIBUTE_TYPE, OPTION_KNIT))
        .toList();
  }

  private boolean hasAttributeOption(Clothing clothing, String attributeName, String optionValue) {
    return clothing.getAttributeValues().stream()
        .anyMatch(value ->
            value.getAttribute().getName().equals(attributeName) &&
                value.getOption().getValue().equals(optionValue)
        );
  }
}
