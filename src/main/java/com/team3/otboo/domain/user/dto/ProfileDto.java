package com.team3.otboo.domain.user.dto;

import com.team3.otboo.domain.weather.dto.WeatherAPILocation;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class ProfileDto {
    private UUID userId;
    private String name;
    private String gender;
    private LocalDate birthDate;
    private WeatherAPILocation location;
    private Integer temperatureSensitivity;
    private String profileImageUrl;
}
