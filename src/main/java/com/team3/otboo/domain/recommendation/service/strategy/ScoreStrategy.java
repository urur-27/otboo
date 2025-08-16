package com.team3.otboo.domain.recommendation.service.strategy;

import com.team3.otboo.domain.clothing.entity.Clothing;
import com.team3.otboo.domain.clothing.mapper.ClothingMapper;
import com.team3.otboo.domain.recommendation.dto.RecommendationDto;
import com.team3.otboo.domain.recommendation.repository.RecommendationRuleRepository;
import com.team3.otboo.domain.user.dto.ProfileDto;
import com.team3.otboo.domain.weather.dto.WeatherDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("scoringStrategy")
@RequiredArgsConstructor
public class ScoreStrategy implements RecommendationStrategy{

  private final RecommendationRuleRepository ruleRepository;
  private final ClothingMapper clothingMapper;

  @Override
  public RecommendationDto recommend(ProfileDto profile, WeatherDto weather,
      List<Clothing> clothes) {
    return null;
  }
}
