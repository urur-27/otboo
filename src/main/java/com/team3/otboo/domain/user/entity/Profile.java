package com.team3.otboo.domain.user.entity;

import com.team3.otboo.domain.base.entity.BaseEntity;
import com.team3.otboo.domain.user.enums.Gender;
import com.team3.otboo.domain.user.enums.Location;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Profile extends BaseEntity {

    // BaseEntity의 상속을 받지 않음
    // user PK를 profile PK로 사용하도록 설정
    @Id
    @Column(name = "user_id")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId                             // User Id를 Profile Id에 매핑
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private LocalDate birthDate;

    @Embedded
    private Location location;

    private Integer temperatureSensitivity;

    private String profileImageUrl;

    @Builder
    private Profile (Gender gender, LocalDate birthDate, Location location, Integer temperatureSensitivity, String profileImageUrl) {
        this.gender = gender;
        this.birthDate = birthDate;
        this.location = location;
        this.temperatureSensitivity = temperatureSensitivity;
        this.profileImageUrl = profileImageUrl;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
