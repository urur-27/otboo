package com.team3.otboo.domain.weather.service;

import com.team3.otboo.domain.weather.dto.WeatherDto;
import com.team3.otboo.domain.weather.dto.request.LocationRequest;
import com.team3.otboo.domain.weather.dto.response.LocationResponse;

import java.util.UUID;

public interface WeatherService {

  WeatherDto getWeatherForUser(UUID userId);

  LocationResponse getLocationForUser(LocationRequest locationRequest);
}
