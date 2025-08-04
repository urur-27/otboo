package com.team3.otboo.domain.recommendation.repository;

import com.team3.otboo.domain.recommendation.entity.RecommendationRule;
import com.team3.otboo.domain.weather.dto.WeatherDto;
import java.util.List;

public interface RecommendationRuleRepositoryCustom {
  List<RecommendationRule> findMatchingRules(WeatherDto weather);
}
