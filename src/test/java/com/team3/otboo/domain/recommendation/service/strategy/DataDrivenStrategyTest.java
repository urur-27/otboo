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
import com.team3.otboo.domain.weather.entity.Temperature;
import com.team3.otboo.domain.weather.entity.Weather;
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
    // given:
    TestData testData = createTestDataForHotDay();

    when(ruleRepository.findMatchingRules(testData.weather())).thenReturn(List.of(testData.rule()));

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

  @Test
  @DisplayName("16~24도의 적당한 날씨에는 맨투맨을 추천해야 한다")
  void recommendSweatshirtOnMildWeather() {
    // given:
    User mockOwner = UserFixture.createDefaultUser();

    CreatedClothing sweatshirt = ClothingFixture.createSweatshirt(mockOwner);
    List<Clothing> userClothes = List.of(sweatshirt.clothing());

    ProfileDto profile = ProfileDto.builder()
        .userId(mockOwner.getId())
        .build();

    // 👇 1. 테스트 시나리오에 맞는 'Weather 엔티티'를 먼저 만듭니다.
    Weather mockWeatherEntity = Weather.builder()
        .id(UUID.randomUUID())
        .temperature(new Temperature(20.0, 0.0, 18.0, 22.0))
        .build();

    // 👇 2. 엔티티를 사용해 'from()' 메서드로 DTO를 생성합니다.
    WeatherDto weather = WeatherDto.from(mockWeatherEntity);

    RecommendationRule mildWeatherRule = RecommendationRule.builder()
        .ruleName("적당한 날씨 규칙")
        .minTemp(16.0)
        .maxTemp(24.9)
        .build();

    RuleToOption ruleToOptionLink = RuleToOption.builder()
        .rule(mildWeatherRule)
        .option(sweatshirt.option())
        .build();
    mildWeatherRule.getRecommendedOptions().add(ruleToOptionLink);

    when(ruleRepository.findMatchingRules(any(WeatherDto.class))).thenReturn(List.of(mildWeatherRule));

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

  // private 헬퍼 메서드도 동일하게 수정합니다.
  private TestData createTestDataForHotDay() {
    User mockOwner = UserFixture.createDefaultUser();

    CreatedClothing createdTshirt = ClothingFixture.createTshirt(mockOwner);
    Clothing tshirt = createdTshirt.clothing();
    AttributeOption tshirtOpt = createdTshirt.option();
    List<Clothing> userClothes = List.of(tshirt);

    ProfileDto profile = ProfileDto.builder()
        .userId(mockOwner.getId())
        .build();

    // 👇 1. 'Weather 엔티티' 생성
    Weather mockWeatherEntity = Weather.builder()
        .id(UUID.randomUUID())
        .temperature(new Temperature(28.0, 0.0, 25.0, 30.0))
        .build();

    // 👇 2. 'from()' 메서드로 DTO 생성
    WeatherDto weather = WeatherDto.from(mockWeatherEntity);

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

  private record TestData(List<Clothing> userClothes, ProfileDto profile, WeatherDto weather, RecommendationRule rule) {}
}
