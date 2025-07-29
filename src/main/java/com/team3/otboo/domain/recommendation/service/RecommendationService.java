package com.team3.otboo.domain.recommendation.service;

import com.team3.otboo.domain.clothing.service.ClothingService;
import com.team3.otboo.domain.recommendation.dto.RecommendationDto;
import com.team3.otboo.domain.recommendation.service.strategy.RecommendationStrategy;
import com.team3.otboo.domain.user.service.ProfileService;
import com.team3.otboo.domain.weather.service.WeatherService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecommendationService {
  private final ProfileService profileService;
  private final ClothingService clothesService;
  private final WeatherService weatherService;
  private final RecommendationStrategy recommendationStrategy;

  public RecommendationDto recommend(UUID userId) {
    return null;
  }
}
