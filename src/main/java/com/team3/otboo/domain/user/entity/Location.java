package com.team3.otboo.domain.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@Embeddable
public class Location {
    private Double latitude;
    private Double longitude;
    private Integer x;  // 기상청 x 좌표
    private Integer y;  // 기상청 y 좌표

    @ElementCollection
    @CollectionTable(name = "profile_location_names", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "location_name")
    private List<String> locationNames = new ArrayList<>();

    public Location(Double latitude, Double longitude, Integer x, Integer y, List<String> locationNames) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.x = x;
        this.y = y;
        this.locationNames = locationNames;
    }
}
