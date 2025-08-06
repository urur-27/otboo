package com.team3.otboo.domain.recommendation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.team3.otboo.domain.clothing.entity.Clothing;
import com.team3.otboo.domain.clothing.service.ClothingService;
import com.team3.otboo.domain.recommendation.dto.RecommendationDto;
import com.team3.otboo.domain.recommendation.service.strategy.RecommendationStrategy;
import com.team3.otboo.domain.user.dto.ProfileDto;
import com.team3.otboo.domain.user.entity.User;
import com.team3.otboo.domain.user.repository.UserRepository;
import com.team3.otboo.domain.user.service.ProfileService;
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
  RecommendationService recommendationService;

  @Mock
  UserRepository userRepository;
  @Mock
  ProfileService profileService;
  @Mock
  ClothingService clothingService;
  @Mock
  WeatherService weatherService;
  @Mock
  RecommendationStrategy recommendationStrategy;

  @Test
  @DisplayName("추천 서비스는 각 협력 서비스를 올바르게 호출하고 결과를 반환한다")
  void recommend_Success() {
    // given:
    UUID weatherId = UUID.randomUUID();

    User mockUser = UserFixture.createDefaultUser();
    UUID mockUserId = mockUser.getId();
    when(userRepository.findById(mockUserId)).thenReturn(Optional.of(mockUser));

    ProfileDto mockProfile = ProfileDto.builder().userId(mockUserId).build();
    when(profileService.getProfile(mockUserId)).thenReturn(mockProfile);

    List<CreatedClothing> mockCreatedClothes = List.of(ClothingFixture.createTshirt(mockUser),
        ClothingFixture.createShorts(mockUser));
    List<Clothing> mockClothes = mockCreatedClothes.stream()
        .map(CreatedClothing::clothing).toList();
    when(clothingService.getClothesByOwner(mockUser)).thenReturn(mockClothes);

    WeatherDto mockWeather = WeatherDto.builder().id(weatherId).build();
    when(weatherService.getWeatherById(weatherId)).thenReturn(mockWeather);

    RecommendationDto expectedResult = new RecommendationDto(weatherId, mockUserId, List.of());
    when(recommendationStrategy.recommend(mockProfile, mockWeather, mockClothes)).thenReturn(expectedResult);

    // when:
    RecommendationDto actualResult = recommendationService.recommend(weatherId, mockUserId);

    // then:
    assertThat(actualResult).isEqualTo(expectedResult);
  }
}
