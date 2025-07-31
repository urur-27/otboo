package com.team3.otboo.domain.user.entity;

import jakarta.persistence.*;

@Embeddable
public class Location {
    private Double latitude;
    private Double longitude;
    private Integer x;  // 기상청 x 좌표
    private Integer y;  // 기상청 y 좌표
}
