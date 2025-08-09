package com.team3.otboo.domain.weather.controller;


import com.team3.otboo.domain.weather.dto.request.LocationRequest;
import com.team3.otboo.domain.weather.dto.response.LocationResponse;
import com.team3.otboo.domain.weather.service.WeatherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/weathers")
public class WeatherController implements WeatherContentApi {

    private final WeatherService weatherService;

    @GetMapping
    public ResponseEntity<LocationResponse> getWeather(
            @Validated @ModelAttribute LocationRequest locationRequestDto
    ){
        
        return null;
    }

    @GetMapping("/location")
    public ResponseEntity<LocationResponse> getLocation(
            @Validated @ModelAttribute LocationRequest locationRequestDto
    ){
        return ResponseEntity.ok(weatherService.getLocationForUser(locationRequestDto));
    }

    // 매일 02:10, 05:10, 08:10, 11:10, 14:10, 17:10, 20:10, 23:10 (KST)
    @Scheduled(cron = "${scheduler.weather.cron:0 10 2,5,8,11,14,17,20,23 * * *}", zone = "Asia/Seoul")
    public void collectWeatherDataJob() {
        long t0 = System.currentTimeMillis();
        try {
            weatherService.collectWeatherData();
            log.info("[WEATHER] collect done ({} ms)", System.currentTimeMillis() - t0);
        } catch (Exception e) {
            log.error("[WEATHER] collect failed", e);
        }
    }

}
