package com.team3.otboo.domain.user.entity;

import com.team3.otboo.domain.base.entity.BaseEntity;
import com.team3.otboo.domain.user.enums.Gender;
import com.team3.otboo.storage.entity.BinaryContent;
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

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", columnDefinition = "uuid")
    private BinaryContent binaryContent;

    @Builder
    private Profile (Gender gender, LocalDate birthDate, Location location, Integer temperatureSensitivity, BinaryContent binaryContent, User user) {
        this.gender = gender;
        this.birthDate = birthDate;
        this.location = location;
        this.temperatureSensitivity = temperatureSensitivity;
        this.user = user;
        this.binaryContent = binaryContent;
    }

    public static Profile of(Gender gender, LocalDate birthDate, Location location, Integer temperatureSensitivity, BinaryContent binaryContent, User user) {
        return Profile.builder()
                .gender(gender)
                .birthDate(birthDate)
                .location(location)
                .temperatureSensitivity(temperatureSensitivity)
                .binaryContent(binaryContent)
                .user(user)
                .build();
    }

    public void setUser(User user) {
        this.user = user;
        if (user != null && user.getProfile() != this) {
            user.setProfile(this);
        }
    }

    public void update(String newName, Gender newGender, LocalDate newLocalDate, Location newLocation, Integer newTemperatureSensitivity, BinaryContent newBinaryContent) {
        if (newName != null && !newName.equals(this.user.getUsername())) {
            this.user.updateUserName(newName);
        }
        if (newGender != null && !newGender.equals(this.gender)) {
            this.gender = newGender;
        }
        if (newLocalDate != null && !newLocalDate.equals(this.birthDate)) {
            this.birthDate = newLocalDate;
        }

        if (newLocation != null && !newLocation.equals(this.location)) {
            this.location = newLocation;
        }

        if (newTemperatureSensitivity != null && !newTemperatureSensitivity.equals(this.temperatureSensitivity)) {
            this.temperatureSensitivity = newTemperatureSensitivity;
        }

        if (newBinaryContent != null) {
            this.binaryContent = newBinaryContent;
        }
    }
}
