package com.team3.otboo.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.team3.otboo.domain.clothing.entity.Clothing;
import com.team3.otboo.domain.clothing.service.ClothingService;
import com.team3.otboo.domain.feed.dto.OotdDto;
import com.team3.otboo.domain.recommendation.dto.RecommendationDto;
import com.team3.otboo.domain.recommendation.service.RecommendationService;
import com.team3.otboo.domain.recommendation.service.strategy.RecommendationStrategy;
import com.team3.otboo.domain.user.dto.ProfileDto;
import com.team3.otboo.domain.user.entity.User;
import com.team3.otboo.domain.user.repository.UserRepository;
import com.team3.otboo.domain.user.service.ProfileService;
import com.team3.otboo.domain.weather.dto.TemperatureDto;
import com.team3.otboo.domain.weather.dto.WeatherDto;
import com.team3.otboo.domain.weather.service.WeatherService;
import com.team3.otboo.fixture.ClothingFixture;
import com.team3.otboo.fixture.UserFixture;
import java.util.List;
import java.util.Optional;
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
  private UserRepository userRepository;
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
    // given: 더운 날씨 상황 준비
    User mockOwner = UserFixture.createDefaultUser();
    UUID userId = mockOwner.getId();
    when(userRepository.findById(userId)).thenReturn(Optional.of(mockOwner));

    // 테스트에 필요한 옷들 준비
    Clothing tshirt = ClothingFixture.createTshirt(mockOwner);
    Clothing shorts = ClothingFixture.createShorts(mockOwner);
    List<Clothing> mockClothesList = List.of(tshirt, shorts);
    when(clothesService.getClothesByOwner(mockOwner)).thenReturn(mockClothesList);

    ProfileDto mockProfile = ProfileDto.builder().userId(userId).temperatureSensitivity(5).build();
    when(profileService.getProfile(userId)).thenReturn(mockProfile);

    WeatherDto mockWeather = WeatherDto.builder()
        .id(UUID.randomUUID())
        .temperature(TemperatureDto.builder().current(28.0).build())
        .build();
    when(weatherService.getWeatherForUser(userId)).thenReturn(mockWeather);

    RecommendationDto expectedResult = new RecommendationDto(mockWeather.getId(), userId, List.of(/* OotdDto list */));
    when(recommendationStrategy.recommend(mockProfile, mockWeather, mockClothesList)).thenReturn(expectedResult);

    // when:
    RecommendationDto actualResult = recommendationService.recommend(userId);

    // then:
    assertThat(actualResult).isEqualTo(expectedResult);
  }
}
