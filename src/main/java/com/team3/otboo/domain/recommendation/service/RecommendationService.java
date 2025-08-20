package com.team3.otboo.domain.recommendation.service;

import com.team3.otboo.domain.clothing.entity.Clothing;
import com.team3.otboo.domain.clothing.service.ClothingService;
import com.team3.otboo.domain.recommendation.dto.RecommendationDto;
import com.team3.otboo.domain.recommendation.service.strategy.RecommendationStrategy;
import com.team3.otboo.domain.user.dto.ProfileDto;
import com.team3.otboo.domain.user.entity.User;
import com.team3.otboo.domain.user.repository.UserRepository;
import com.team3.otboo.domain.user.service.ProfileService;
import com.team3.otboo.domain.weather.dto.WeatherDto;
import com.team3.otboo.domain.weather.service.WeatherService;
import com.team3.otboo.global.exception.user.UserNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class RecommendationService {

  private final UserRepository userRepository;
  private final ProfileService profileService;
  private final ClothingService clothesService;
  private final WeatherService weatherService;
  private final RecommendationStrategy recommendationStrategy;

  public RecommendationService(UserRepository userRepository, ProfileService profileService,
      ClothingService clothesService, WeatherService weatherService,
      @Qualifier("scoringStrategy")
      RecommendationStrategy recommendationStrategy) {
    this.userRepository = userRepository;
    this.profileService = profileService;
    this.clothesService = clothesService;
    this.weatherService = weatherService;
    this.recommendationStrategy = recommendationStrategy;
  }

  public RecommendationDto recommend(UUID weatherId, UUID userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(UserNotFoundException::new);

    ProfileDto profile = profileService.getProfile(userId);
    List<Clothing> clothes = clothesService.getClothesByOwner(user);
    WeatherDto weather = weatherService.getWeatherById(weatherId);

    return recommendationStrategy.recommend(profile, weather, clothes);
  }
}
