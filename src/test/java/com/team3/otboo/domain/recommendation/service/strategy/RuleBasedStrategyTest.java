package com.team3.otboo.domain.recommendation.service.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import com.team3.otboo.domain.clothing.entity.Clothing;
import com.team3.otboo.domain.clothing.mapper.ClothingMapper;
import com.team3.otboo.domain.feed.dto.OotdDto;
import com.team3.otboo.domain.recommendation.dto.RecommendationDto;
import com.team3.otboo.domain.user.dto.ProfileDto;
import com.team3.otboo.domain.user.entity.User;
import com.team3.otboo.domain.weather.dto.TemperatureDto;
import com.team3.otboo.domain.weather.dto.WeatherDto;
import com.team3.otboo.fixture.ClothingFixture;
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
public class RuleBasedStrategyTest {

  @InjectMocks
  private RuleBasedStrategy ruleBasedStrategy;

  @Mock
  ClothingMapper clothingMapper;

  @Test
  @DisplayName("기온이 15도 이하인 쌀쌀한 날씨에는 긴팔니틀를 추천해야 한다")
  void recommendKnitOnCoolWeather() {
    // given:
    User mockOwner = UserFixture.createDefaultUser();

    // 니트를 추천하고, 반팔티는 추천x
    Clothing tshirt = ClothingFixture.createTshirt(mockOwner);
    Clothing knit = ClothingFixture.createKnit(mockOwner);
    List<Clothing> userClothes = List.of(tshirt, knit);

    ProfileDto profile = ProfileDto.builder()
        .userId(mockOwner.getId())
        .temperatureSensitivity(3)
        .build();

    // 쌀쌀한 날씨 정의 (기온 15도)
    WeatherDto weather = WeatherDto.builder()
        .id(UUID.randomUUID())
        .temperature(TemperatureDto.builder()
            .current(15.0)
            .build())
        .build();

    // 가짜 ClothingMapper 동작 정의
    when(clothingMapper.toOotdDtoList(anyList())).thenAnswer(invoation -> {
      List<Clothing> inputList = invoation.getArgument(0);
      return inputList.stream()
          .map(c -> new OotdDto(c.getId(), c.getName(), c.getImageUrl(), null, null))
          .toList();
    });

    // when:
    RecommendationDto actualResult = ruleBasedStrategy.recommend(profile, weather, userClothes);

    // then: 겨울니트 하나만 추천되었는지 검증
    assertThat(actualResult).isNotNull();
    assertThat(actualResult.clothes()).hasSize(1);
    assertThat(actualResult.clothes().getFirst().name()).isEqualTo("겨울니트");
  }
}
