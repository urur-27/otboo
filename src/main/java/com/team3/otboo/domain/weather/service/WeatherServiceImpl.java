package com.team3.otboo.domain.weather.service;

import com.team3.otboo.domain.weather.dto.WeatherDto;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class WeatherServiceImpl implements WeatherService{

  @Override
  public WeatherDto getWeatherById(UUID weatherId) {
    return null;
  }
}
