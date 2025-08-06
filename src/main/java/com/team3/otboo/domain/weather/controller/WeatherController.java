package com.team3.otboo.domain.weather.controller;


import com.team3.otboo.domain.weather.dto.request.LocationRequest;
import com.team3.otboo.domain.weather.dto.response.LocationResponse;
import com.team3.otboo.domain.weather.service.WeatherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/weathers")
public class WeatherController implements WeatherContentApi {

    private final WeatherService weatherService;

    public ResponseEntity<LocationResponse> getWeather(
            @Validated @ModelAttribute LocationRequest locationRequestDto
    ){
        return ResponseEntity.ok(weatherService.getLocationForUser(locationRequestDto));
    }

    @GetMapping("/location")
    public ResponseEntity<LocationResponse> getLocation(
            @Validated @ModelAttribute LocationRequest locationRequestDto
    ){
        return ResponseEntity.ok(weatherService.getLocationForUser(locationRequestDto));
    }

}
