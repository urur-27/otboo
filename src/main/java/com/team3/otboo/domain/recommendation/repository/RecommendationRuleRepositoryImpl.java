package com.team3.otboo.domain.recommendation.repository;

import static com.team3.otboo.domain.recommendation.entity.QRecommendationRule.recommendationRule;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.team3.otboo.domain.recommendation.entity.RecommendationRule;
import com.team3.otboo.domain.weather.dto.WeatherDto;
import com.team3.otboo.domain.weather.enums.PrecipitationType;
import com.team3.otboo.domain.weather.enums.SkyStatus;
import com.team3.otboo.domain.weather.enums.WindSpeedLevel;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RecommendationRuleRepositoryImpl implements RecommendationRuleRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<RecommendationRule> findMatchingRules(WeatherDto weather) {
    BooleanBuilder builder = new BooleanBuilder();

    // 1. 온도 조건
    if (weather.getTemperature() != null && weather.getTemperature().getCurrent() != null) {
      Double currentTemp = weather.getTemperature().getCurrent();
      builder.and(recommendationRule.minTemp.loe(currentTemp).or(recommendationRule.minTemp.isNull()));
      builder.and(recommendationRule.maxTemp.goe(currentTemp).or(recommendationRule.maxTemp.isNull()));
    }

    // 2. 하늘 상태 조건
    if (weather.getSkyStatus() != null) {
      SkyStatus skyStatus = SkyStatus.valueOf(weather.getSkyStatus());
      builder.and(recommendationRule.skyStatus.eq(skyStatus).or(recommendationRule.skyStatus.isNull()));
    }

    // 3. 강수 형태 조건
    if (weather.getPrecipitation() != null && weather.getPrecipitation().getType() != null) {
      PrecipitationType precipitationType = PrecipitationType.valueOf(weather.getPrecipitation().getType());
      builder.and(recommendationRule.precipitationType.eq(precipitationType)
          .or(recommendationRule.precipitationType.isNull()));
    }

    // 4. 풍속 조건
    if (weather.getWindSpeed() != null && weather.getWindSpeed().getAsWord() != null) {
      WindSpeedLevel windSpeedLevel = WindSpeedLevel.valueOf(weather.getWindSpeed().getAsWord());
      builder.and(recommendationRule.windSpeedLevel.eq(windSpeedLevel).or(recommendationRule.windSpeedLevel.isNull()));
    }

    // 5. 습도 조건
    if (weather.getHumidity() != null && weather.getHumidity().getCurrent() != null) {
      Double currentHumidity = weather.getHumidity().getCurrent();
      builder.and(recommendationRule.minHumidity.loe(currentHumidity).or(recommendationRule.minHumidity.isNull()));
      builder.and(recommendationRule.maxHumidity.goe(currentHumidity).or(recommendationRule.maxHumidity.isNull()));
    }

    return queryFactory
        .selectFrom(recommendationRule)
        .where(builder)
        .fetch();
  }
}
