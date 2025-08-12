package com.team3.otboo.domain.recommendation.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.team3.otboo.config.QuerydslConfig;
import com.team3.otboo.domain.clothing.repository.AttributeRepository;
import com.team3.otboo.domain.recommendation.entity.RecommendationRule;
import com.team3.otboo.domain.weather.dto.PrecipitationDto;
import com.team3.otboo.domain.weather.dto.TemperatureDto;
import com.team3.otboo.domain.weather.dto.WeatherDto;
import com.team3.otboo.domain.weather.entity.Precipitation;
import com.team3.otboo.domain.weather.entity.Temperature;
import com.team3.otboo.domain.weather.entity.Weather;
import com.team3.otboo.domain.weather.enums.PrecipitationType;
import com.team3.otboo.domain.weather.enums.SkyStatus;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@Import(QuerydslConfig.class)
@DataJpaTest
public class RecommendationRuleRepositoryTest {

  @Autowired
  private RecommendationRuleRepository recommendationRuleRepository;

  @Autowired
  private AttributeRepository attributeRepository;

  @Test
  @DisplayName("온도, 습도, 하늘 상태 등 복합적인 조건에 맞는 규칙을 정확히 찾아낸다")
  void findMatchingRules_withComplexConditions() {
    //given: DB에 규칙을 미리 저장
    RecommendationRule hotAndClearRule = RecommendationRule.builder()
        .ruleName("덥고 맑은 날")
        .minTemp(25.0)
        .skyStatus(SkyStatus.CLEAR)
        .build();

    RecommendationRule coolAndSnowRule = RecommendationRule.builder()
        .ruleName("춥고 눈 오는 날")
        .maxTemp(0.0)
        .precipitationType(PrecipitationType.SNOW)
        .build();

    recommendationRuleRepository.saveAll(List.of(hotAndClearRule, coolAndSnowRule));

    // when:
    Weather mockWeatherEntity = Weather.builder()
        .temperature(new Temperature(-2.0, null, null, null))
        .precipitation(new Precipitation(PrecipitationType.SNOW, null, null))
        .skyStatus(SkyStatus.CLOUDY)
        .build();

    WeatherDto snowyWeatherDto = WeatherDto.from(mockWeatherEntity);

    List<RecommendationRule> result = recommendationRuleRepository.findMatchingRules(snowyWeatherDto);

    // then: '춥고 눈 오는 날' 규칙 하나만 조회되어야 함
    assertThat(result).hasSize(1);
    assertThat(result.getFirst().getRuleName()).isEqualTo("춥고 눈 오는 날");
  }
}
