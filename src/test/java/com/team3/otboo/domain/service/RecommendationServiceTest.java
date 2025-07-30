package com.team3.otboo.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.team3.otboo.domain.clothing.entity.Attribute;
import com.team3.otboo.domain.clothing.entity.AttributeOption;
import com.team3.otboo.domain.clothing.entity.Clothing;
import com.team3.otboo.domain.clothing.service.ClothingService;
import com.team3.otboo.domain.recommendation.dto.RecommendationDto;
import com.team3.otboo.domain.recommendation.service.RecommendationService;
import com.team3.otboo.domain.recommendation.service.strategy.RecommendationStrategy;
import com.team3.otboo.domain.user.dto.ProfileDto;
import com.team3.otboo.domain.user.entity.User;
import com.team3.otboo.domain.user.service.ProfileService;
import com.team3.otboo.domain.weather.dto.TemperatureDto;
import com.team3.otboo.domain.weather.dto.WeatherDto;
import com.team3.otboo.domain.weather.service.WeatherService;
import com.team3.otboo.fixture.ClothingFixture;
import com.team3.otboo.fixture.UserFixture;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

  @InjectMocks
  private RecommendationService recommendationService;

  @Mock
  private ProfileService profileService;
  @Mock
  private ClothingService clothesService;
  @Mock
  private WeatherService weatherService;
  @Mock
  private RecommendationStrategy recommendationStrategy;

  @Test
  void 맑고_더운_날에는_반팔과_반바지를_추천한다() {
    User mockOwner = UserFixture.createDefaultUser();
    Clothing tshirt = ClothingFixture.createTshirt(mockOwner);
    Clothing shorts = ClothingFixture.createShorts(mockOwner);
    Clothing knit = ClothingFixture.createKnit(mockOwner);

    List<Clothing> mockClothesList = List.of(tshirt, shorts, knit);
    when(clothesService.getClothesByOwner(any(User.class))).thenReturn(mockClothesList);

    ProfileDto mockProfile = ProfileDto.builder()
        .temperatureSensitivity(5)
        .build();
    TemperatureDto mockTemp = TemperatureDto.builder()
        .current(28.0)
        .build();
    WeatherDto mockWeather = WeatherDto.builder()
        .skyStatus("맑음")
        .temperature(mockTemp)
        .build();
    when(profileService.getProfile(any(UUID.class))).thenReturn(mockProfile);
    when(weatherService.getWeatherForUser(any(UUID.class))).thenReturn(mockWeather);

    List<Clothing> recommendedItems = List.of(tshirt, shorts);
    RecommendationDto expectedResult = new RecommendationDto(recommendedItems, "더운 날씨에 딱 맞네요!");
    when(recommendationStrategy.recommend(mockProfile, mockWeather, mockClothesList)).thenReturn(expectedResult);

    RecommendationDto actualResult = recommendationService.recommend(mockOwner.getId());

    assertThat(actualResult).isEqualTo(expectedResult);
    assertThat(actualResult.recommendedItems())
        .hasSize(2)
        .extracting(Clothing::getName)
        .containsExactlyInAnyOrder("검은색 반팔티", "청반바지");
  }
}
