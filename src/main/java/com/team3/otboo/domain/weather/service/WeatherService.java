package com.team3.otboo.domain.weather.service;

import com.team3.otboo.domain.weather.dto.WeatherDto;
import java.util.UUID;

public interface WeatherService {

  WeatherDto getWeatherForUser(UUID userId);
}
