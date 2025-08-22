package com.team3.otboo.domain.recommendation;

import static org.assertj.core.api.Assertions.assertThat;

import com.team3.otboo.domain.clothing.entity.Attribute;
import com.team3.otboo.domain.clothing.entity.AttributeOption;
import com.team3.otboo.domain.clothing.entity.Clothing;
import com.team3.otboo.domain.clothing.entity.ClothingAttributeValue;
import com.team3.otboo.domain.clothing.repository.AttributeOptionRepository;
import com.team3.otboo.domain.clothing.repository.AttributeRepository;
import com.team3.otboo.domain.clothing.repository.ClothingRepository;
import com.team3.otboo.domain.recommendation.dto.RecommendationDto;
import com.team3.otboo.domain.recommendation.entity.RecommendationRule;
import com.team3.otboo.domain.recommendation.entity.RuleToOption;
import com.team3.otboo.domain.recommendation.repository.RecommendationRuleRepository;
import com.team3.otboo.domain.recommendation.service.RecommendationService;
import com.team3.otboo.domain.user.entity.User;
import com.team3.otboo.domain.user.repository.UserRepository;
import com.team3.otboo.domain.weather.entity.Temperature;
import com.team3.otboo.domain.weather.entity.Weather;
import com.team3.otboo.domain.weather.repository.WeatherRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class RecommendationIntegrationTest {
  @Autowired
  private RecommendationService recommendationService;
  // --- 테스트 데이터 저장을 위한 Repository들 ---
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private ClothingRepository clothingRepository;
  @Autowired
  private AttributeRepository attributeRepository;
  @Autowired
  private AttributeOptionRepository attributeOptionRepository;
  @Autowired
  private RecommendationRuleRepository recommendationRuleRepository;
  @Autowired
  private WeatherRepository weatherRepository;

  @Test
  @DisplayName("점수제 기반 추천: 더운 날씨에 반팔티는 추천하고 긴팔니트는 추천하지 않아야 한다")
  void recommendation_withScoringSystem() {
    // given: DB에 테스트 시나리오를 구성합니다.
    // 1. 속성 정의
    Attribute typeAttribute = attributeRepository.save(Attribute.of("종류", List.of("반팔티", "긴팔니트")));
    AttributeOption tshirtOption = typeAttribute.getOptions().get(0);
    AttributeOption knitOption = typeAttribute.getOptions().get(1);

    // 2. 사용자 및 옷장 구성 (수정된 부분)
    User user = userRepository.save(User.builder().username("testuser").email("test@test.com").build());

    // --- 나의 반팔티 생성 및 속성 연결 ---
    Clothing tshirt = Clothing.of("나의 반팔티", user);
    // 👇 '반팔티'와 '종류' 속성을 연결하는 ClothingAttributeValue를 생성합니다.
    ClothingAttributeValue.of(tshirt, typeAttribute, tshirtOption); // (of 메서드가 있다고 가정)
    clothingRepository.save(tshirt); // 👇 CascadeType.ALL 때문에 Clothing만 저장해도 연결고리가 함께 저장됩니다.

    // --- 나의 긴팔니트 생성 및 속성 연결 ---
    Clothing knit = Clothing.of("나의 긴팔니트", user);
    // 👇 '긴팔니트'와 '종류' 속성을 연결하는 ClothingAttributeValue를 생성합니다.
    ClothingAttributeValue.of(knit, typeAttribute, knitOption);
    clothingRepository.save(knit);

    // 3. 추천 규칙 구성 (긍정/부정 점수)
    RecommendationRule hotPositiveRule = recommendationRuleRepository.save(
        RecommendationRule.builder().ruleName("더울때 반팔티").minTemp(25.0).score(50).build()
    );
    hotPositiveRule.getRecommendedOptions().add(
        RuleToOption.builder().rule(hotPositiveRule).option(tshirtOption).build());

    RecommendationRule hotNegativeRule = recommendationRuleRepository.save(
        RecommendationRule.builder().ruleName("더울때 긴팔니트").minTemp(25.0).score(-100).build()
    );
    hotNegativeRule.getRecommendedOptions().add(RuleToOption.builder().rule(hotNegativeRule).option(knitOption).build());

    // 4. 테스트용 날씨 데이터 생성
    Weather hotWeather = weatherRepository.save(
        Weather.builder().temperature(new Temperature(28.0, 0.0, 25.0, 30.0)).build()
    );

    // when: 추천 서비스를 호출합니다.
    RecommendationDto result = recommendationService.recommend(user.getId(), hotWeather.getId());

    // then: 결과를 검증합니다.
    assertThat(result.clothes()).hasSize(1); // 상의 1개만 추천되어야 함
    assertThat(result.clothes().getFirst().name()).isEqualTo("나의 반팔티"); // 추천된 옷은 '반팔티'여야 함
  }
}
