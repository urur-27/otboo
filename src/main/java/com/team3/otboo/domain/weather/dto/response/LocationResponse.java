package com.team3.otboo.domain.weather.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LocationResponse {
    private Double latitude;

    private Double longitude;

    private Integer x;

    private Integer y;

    private List<String> locationNames;

    @Builder
    private LocationResponse(Double latitude, Double longitude, Integer x, Integer y, List<String> locationNames) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.x = x;
        this.y = y;
        this.locationNames = locationNames;
    }

    public static LocationResponse of(Double latitude, Double longitude, Integer x, Integer y, List<String> locationNames) {
        return LocationResponse.builder()
                .latitude(latitude)
                .longitude(longitude)
                .x(x)
                .y(y)
                .locationNames(locationNames)
                .build();
    }
}
