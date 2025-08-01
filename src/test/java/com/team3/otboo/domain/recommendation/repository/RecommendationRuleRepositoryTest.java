package com.team3.otboo.domain.recommendation.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.team3.otboo.config.QuerydslConfig;
import com.team3.otboo.domain.clothing.repository.AttributeRepository;
import com.team3.otboo.domain.recommendation.entity.RecommendationRule;
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
  @DisplayName("현재 기온이 규칙의 범위 안에 있을 때 해당 규칙을 올바르게 찾아낸다")
  void findingMatchRules_Success() {
    //given: DB에 규칙을 미리 저장
    RecommendationRule hotRule = RecommendationRule.builder()
        .ruleName("더운 날")
        .minTemp(25.0)
        .maxTemp(null)
        .skyStatus(SkyStatus.CLEAR)
        .build();
    RecommendationRule coolRule = RecommendationRule.builder()
        .ruleName("쌀쌀한 날")
        .minTemp(10.0)
        .maxTemp(15.9)
        .skyStatus(SkyStatus.CLOUDY)
        .build();
    recommendationRuleRepository.saveAll(List.of(hotRule, coolRule));

    // when: 12도의 날씨에 맞는 규칙 조회
    List<RecommendationRule> result = recommendationRuleRepository.findMatchingRules(12.0, SkyStatus.CLOUDY);

    // then: "쌀쌀한 날" 규칙 하나만 조회 되어야함
    assertThat(result.getFirst().getRuleName()).isEqualTo("쌀쌀한 날");
  }
}
