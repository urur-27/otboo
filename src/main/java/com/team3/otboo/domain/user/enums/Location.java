package com.team3.otboo.domain.user.enums;

import jakarta.persistence.*;

import java.util.List;

@Embeddable
public class Location {
    private Double latitude;
    private Double longitude;
    private Integer x;  // 기상청 x 좌표
    private Integer y;  // 기상청 y 좌표

    @ElementCollection
    @CollectionTable(name = "profile_location_names", joinColumns = @JoinColumn(name = "profile_user_id"))
    @Column(name = "location_name")
    private List<String> locationNames;
}
