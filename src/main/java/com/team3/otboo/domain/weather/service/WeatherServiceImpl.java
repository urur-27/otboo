package com.team3.otboo.domain.weather.service;

import com.team3.otboo.domain.weather.dto.KakaoGeoResponse;
import com.team3.otboo.domain.weather.dto.WeatherDto;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import com.team3.otboo.domain.weather.dto.request.LocationRequest;
import com.team3.otboo.domain.weather.dto.response.LocationResponse;
import com.team3.otboo.global.exception.weather.ExternalApiException;
import com.team3.otboo.props.external.ExternalApisProperties;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class WeatherServiceImpl implements WeatherService{

  @Qualifier("locationRestTemplate")
  private final RestTemplate locationRestTemplate;

  private final ExternalApisProperties apisProps;

  @Override
  public WeatherDto getWeatherById(UUID weatherId) {
    return null;
  }

  @Override
  @CircuitBreaker(name = "locationRestTemplate", fallbackMethod = "fallbackLocation")
  public LocationResponse getLocationForUser(LocationRequest locationRequest) {
    String uri = UriComponentsBuilder
      .fromUriString(apisProps.getApis().get("kakao-map").getBaseUrl())
      .queryParam("x", locationRequest.longitude())
      .queryParam("y", locationRequest.latitude())
      .build()
      .toUriString();

    KakaoGeoResponse responseJson = locationRestTemplate.getForObject(uri, KakaoGeoResponse.class);

    if (responseJson.getDocuments().isEmpty()) {
      throw new ExternalApiException();
    }

    var doc = responseJson.getDocuments().get(0);

    List<String> locationNames = Stream.of(
      doc.getRegion1depthName(),
      doc.getRegion2depthName(),
      doc.getRegion3depthName(),
      doc.getRegion4depthName()
    )
    .filter(s -> s != null && !s.isBlank())
    .toList();

    return LocationResponse.of(
      locationRequest.latitude(),
      locationRequest.longitude(),
      doc.getX(), doc.getY(),
      locationNames
    );
  }

  public LocationResponse fallbackLocation(LocationRequest locationRequest, Throwable ex) {
    if (ex instanceof CallNotPermittedException) {
      log.warn("CircuitBreaker OPEN 상태: 호출 차단됨 호출 정보={}, 예외={}", locationRequest, ex.toString());
    } else {
      log.warn("외부 API 호출 실패: 요청 정보={}, 예외={}", locationRequest, ex.toString());
    }
    throw new ExternalApiException();
  }
}
