package com.team3.otboo.domain.user.dto;

import com.team3.otboo.domain.weather.dto.WeatherAPILocation;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class ProfileDto {
    private Long userId;
    private String name;
    private String gender;
    private LocalDate birthDate;
    private WeatherAPILocation location;
    private Integer temperatureSensitivity;
    private String profileImageUrl;
}
