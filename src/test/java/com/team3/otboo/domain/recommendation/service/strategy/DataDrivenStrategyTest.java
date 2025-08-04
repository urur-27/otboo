package com.team3.otboo.domain.recommendation.service.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import com.team3.otboo.domain.clothing.entity.AttributeOption;
import com.team3.otboo.domain.clothing.entity.Clothing;
import com.team3.otboo.domain.clothing.mapper.ClothingMapper;
import com.team3.otboo.domain.feed.dto.OotdDto;
import com.team3.otboo.domain.recommendation.dto.RecommendationDto;
import com.team3.otboo.domain.recommendation.entity.RecommendationRule;
import com.team3.otboo.domain.recommendation.entity.RuleToOption;
import com.team3.otboo.domain.recommendation.repository.RecommendationRuleRepository;
import com.team3.otboo.domain.user.dto.ProfileDto;
import com.team3.otboo.domain.user.entity.User;
import com.team3.otboo.domain.weather.dto.TemperatureDto;
import com.team3.otboo.domain.weather.dto.WeatherDto;
import com.team3.otboo.fixture.ClothingFixture;
import com.team3.otboo.fixture.ClothingFixture.CreatedClothing;
import com.team3.otboo.fixture.UserFixture;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DataDrivenStrategyTest {

  @InjectMocks
  private DataDrivenStrategy dataDrivenStrategy;

  @Mock
  private RecommendationRuleRepository ruleRepository;
  @Mock
  private ClothingMapper clothingMapper;

  @Test
  @DisplayName("DB에 저장된 '더운 날 규칙' 에 따라 반팔티를 추천해야 한다")
  void recommendBasedOnDatabaseRule() {
    // given: 테스트 시나리오에 필요한 데이터를 헬퍼 메서드로 생성
    TestData testData = createTestDataForHotDay();

    // Mock Repository 설정: 변경된 5개 파라미터 형식에 맞게 호출
    when(ruleRepository.findMatchingRules(testData.weather)).thenReturn(List.of(testData.rule()));

    // Mock Mapper 설정
    when(clothingMapper.toOotdDtoList(anyList())).thenAnswer(invocation -> {
      List<Clothing> inputList = invocation.getArgument(0);
      return inputList.stream()
          .map(c -> new OotdDto(c.getId(), c.getName(), c.getImageUrl(), null, null))
          .toList();
    });

    //when:
    RecommendationDto actualResult = dataDrivenStrategy.recommend(
        testData.profile(),
        testData.weather(),
        testData.userClothes()
    );

    //then:
    assertThat(actualResult.clothes())
        .hasSize(1)
        .extracting(OotdDto::name)
        .containsExactly("검은색 반팔티");
  }

  // 테스트 데이터 생성을 위한 private 헬퍼 메서드
  private TestData createTestDataForHotDay() {
    User mockOwner = UserFixture.createDefaultUser();

    ClothingFixture.CreatedClothing createdTshirt = ClothingFixture.createTshirt(mockOwner);
    Clothing tshirt = createdTshirt.clothing();
    AttributeOption tshirtOpt = createdTshirt.option();
    List<Clothing> userClothes = List.of(tshirt);

    ProfileDto profile = ProfileDto.builder()
        .userId(mockOwner.getId())
        .build();

    WeatherDto weather = WeatherDto.builder()
        .id(UUID.randomUUID())
        .temperature(TemperatureDto.builder().current(28.0).build())
        .build();

    RecommendationRule hotWeatherRule = RecommendationRule.builder()
        .ruleName("더운 날 규칙")
        .minTemp(25.0)
        .build();

    RuleToOption ruleToOptionLink = RuleToOption.builder()
        .rule(hotWeatherRule)
        .option(tshirtOpt)
        .build();
    hotWeatherRule.getRecommendedOptions().add(ruleToOptionLink);

    return new TestData(userClothes, profile, weather, hotWeatherRule);
  }

  @Test
  @DisplayName("16~24도의 적당한 날씨에는 맨투맨을 추천해야 한다")
  void recommendSweatshirtOnMildWeather() {
    // given:
    User mockOwner = UserFixture.createDefaultUser();

    CreatedClothing sweatshirt = ClothingFixture.createSweatshirt(mockOwner);
    List<Clothing> userClothes = List.of(sweatshirt.clothing());

    ProfileDto profile = ProfileDto.builder().userId(mockOwner.getId()).build();
    WeatherDto weather = WeatherDto.builder()
        .id(UUID.randomUUID())
        .temperature(TemperatureDto.builder().current(20.0).build())
        .build();

    RecommendationRule mildWeatherRule = RecommendationRule.builder()
        .ruleName("적당한 날씨 규칙")
        .minTemp(16.0)
        .maxTemp(24.9)
        .build();

    // '적당한 날씨 규칙'이 '맨투맨 옵션'을 추천하도록 명시적으로 연결
    RuleToOption ruleToOptionLink = RuleToOption.builder()
        .rule(mildWeatherRule)
        .option(sweatshirt.option())
        .build();
    mildWeatherRule.getRecommendedOptions().add(ruleToOptionLink);

    // Mock Repository 설정
    when(ruleRepository.findMatchingRules(any(WeatherDto.class))).thenReturn(List.of(mildWeatherRule));

    // 가짜 Mapper 설정
    when(clothingMapper.toOotdDtoList(anyList())).thenAnswer(invocation -> {
      List<Clothing> inputList = invocation.getArgument(0);
      return inputList.stream()
          .map(c -> new OotdDto(c.getId(), c.getName(), null, null, null))
          .toList();
    });

    // when:
    RecommendationDto actualResult = dataDrivenStrategy.recommend(profile, weather, userClothes);

    // then:
    assertThat(actualResult.clothes())
        .hasSize(1)
        .extracting(OotdDto::name)
        .containsExactly("곰돌이 맨투맨");
  }

  private record TestData(List<Clothing> userClothes, ProfileDto profile, WeatherDto weather, RecommendationRule rule) {}
}
