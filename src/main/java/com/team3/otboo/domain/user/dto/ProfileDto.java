package com.team3.otboo.domain.user.dto;

import com.team3.otboo.domain.user.entity.Location;
import com.team3.otboo.domain.user.entity.Profile;
import com.team3.otboo.domain.user.enums.Gender;
import com.team3.otboo.domain.weather.dto.WeatherAPILocation;
import java.util.UUID;

import com.team3.otboo.storage.entity.BinaryContent;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class ProfileDto {
    private UUID userId;
    private String name;
    private Gender gender;
    private LocalDate birthDate;
    private Location location;
    private Integer temperatureSensitivity;
    private String profileImageUrl;

    @Builder
    private ProfileDto(UUID userId, String name, Gender gender, LocalDate birthDate, Location location, Integer temperatureSensitivity, String profileImageUrl) {
        this.userId = userId;
        this.name = name;
        this.gender = gender;
        this.birthDate = birthDate;
        this.location = location;
        this.temperatureSensitivity = temperatureSensitivity;
        this.profileImageUrl = profileImageUrl;
    }

    public static ProfileDto of(UUID userId, String name, Gender gender, LocalDate birthDate, Location location, Integer temperatureSensitivity, BinaryContent binaryContent) {
        return ProfileDto.builder()
                .userId(userId)
                .name(name)
                .gender(gender)
                .birthDate(birthDate)
                .location(location)
                .temperatureSensitivity(temperatureSensitivity)
                .profileImageUrl(binaryContent != null ? binaryContent.getImageUrl() : null)
                .build();
    }
}
