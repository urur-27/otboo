package com.team3.otboo.domain.recommendation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
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
import com.team3.otboo.fixture.ClothingFixture.CreatedClothing;
import com.team3.otboo.fixture.UserFixture;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
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
  @DisplayName("맑고 더운 날에는 반팔과 반바지를 추천한다")
  void recommendTshirtAndShortsOnHotWeather() {
    // given: 더운 날씨 상황 준비
    User mockOwner = UserFixture.createDefaultUser();
    UUID userId = mockOwner.getId();
    when(userRepository.findById(userId)).thenReturn(Optional.of(mockOwner));

    // 테스트에 필요한 옷들 준비
    CreatedClothing tshirt = ClothingFixture.createTshirt(mockOwner);
    CreatedClothing shorts = ClothingFixture.createShorts(mockOwner);
    List<Clothing> mockClothesList = List.of(tshirt.clothing(), shorts.clothing());
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

  @Test
  @DisplayName("16~24도의 적당한 날씨에는 맨투맨을 추천해야 한다")
  void recommendSweatshirtOnMildWeather() {
    // given: 적당한 날씨 준비
    User mockOwner = UserFixture.createDefaultUser();
    UUID userId = mockOwner.getId();
    when(userRepository.findById(userId)).thenReturn(Optional.of(mockOwner));

    // 추천 옷
    CreatedClothing sweatshirt = ClothingFixture.createSweatshirt(mockOwner);
    CreatedClothing tshirt = ClothingFixture.createTshirt(mockOwner);
    List<Clothing> mockClothingList = List.of(tshirt.clothing(), sweatshirt.clothing());
    when(clothesService.getClothesByOwner(mockOwner)).thenReturn(mockClothingList);

    ProfileDto mockProfile = ProfileDto.builder()
        .userId(userId)
        .temperatureSensitivity(3)
        .build();
    when(profileService.getProfile(userId)).thenReturn(mockProfile);

    // "적당한 날씨" 정의 (20도)
    WeatherDto mockWeather = WeatherDto.builder()
        .id(mockOwner.getId())
        .temperature(TemperatureDto.builder()
            .current(20.0)
            .build())
        .build();
    when(weatherService.getWeatherForUser(userId)).thenReturn(mockWeather);

    // when: 서비스 호출
    RecommendationDto actualResult = recommendationService.recommend(userId);

    // then: 검증
    assertThat(actualResult.clothes())
        .hasSize(1)
        .extracting(OotdDto::name)
        .containsExactly("곰돌이 맨투맨");
  }
}
