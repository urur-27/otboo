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

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Profile extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)     // User Id를 Profile Id에 매핑
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column
    private LocalDate birthDate;

    @Embedded
    private Location location;

    @Column
    private Integer temperatureSensitivity;

    @Column
    private String profileImageUrl;

    @Builder
    private Profile (Gender gender, LocalDate birthDate, Location location, Integer temperatureSensitivity, String profileImageUrl, User user) {
        this.gender = gender;
        this.birthDate = birthDate;
        this.location = location;
        this.temperatureSensitivity = temperatureSensitivity;
        this.profileImageUrl = profileImageUrl;
        this.user = user;
    }

    public static Profile of(Gender gender, LocalDate birthDate, Location location, Integer temperatureSensitivity, String profileImageUrl, User user) {
        return Profile.builder()
                .gender(gender)
                .birthDate(birthDate)
                .location(location)
                .temperatureSensitivity(temperatureSensitivity)
                .profileImageUrl(profileImageUrl)
                .user(user)
                .build();
    }

    public void setUser(User user) {
        this.user = user;
        if (user != null && user.getProfile() != this) {
            user.setProfile(this);
        }
    }
}
