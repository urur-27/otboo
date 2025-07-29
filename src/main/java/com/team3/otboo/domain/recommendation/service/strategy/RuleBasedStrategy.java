package com.team3.otboo.domain.recommendation.service.strategy;

import com.team3.otboo.domain.clothing.entity.Clothing;
import com.team3.otboo.domain.recommendation.dto.RecommendationDto;
import com.team3.otboo.domain.user.dto.ProfileDto;
import com.team3.otboo.domain.weather.dto.WeatherDto;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class RuleBasedStrategy implements RecommendationStrategy{

  @Override
  public RecommendationDto recommend(ProfileDto profile, WeatherDto weather,
      List<Clothing> clothes) {
    return null;
  }
}
