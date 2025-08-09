package com.team3.otboo.domain.weather.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WeatherLocation {
    private Double latitude;
    private Double longitude;
    private Integer x;  // 기상청 x 좌표
    private Integer y;  // 기상청 y 좌표

    @ElementCollection
    @CollectionTable(name = "weather_location_names", joinColumns = @JoinColumn(name = "weather_id"))
    @Column(name = "location")
    private List<String> locationNames = new ArrayList<>();

    public WeatherLocation(Double latitude, Double longitude, Integer x, Integer y, List<String> locationNames) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.x = x;
        this.y = y;
        this.locationNames.clear();
        this.locationNames.addAll(locationNames);
    }

    /**
     * 다른 인스턴스의 필드 값으로 이 객체를 갱신합니다.
     * 컬렉션은 완전히 교체(클리어+addAll) 합니다.
     */
    public void updateFrom(WeatherLocation other) {
        if (other.latitude != null) {
            this.latitude = other.latitude;
        }
        if (other.longitude != null) {
            this.longitude = other.longitude;
        }
        if (other.x != null) {
            this.x = other.x;
        }
        if (other.y != null) {
            this.y = other.y;
        }
        if (other.locationNames != null) {
            this.locationNames.clear();
            this.locationNames.addAll(other.locationNames);
        }
    }
}
