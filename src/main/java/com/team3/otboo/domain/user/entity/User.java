package com.team3.otboo.domain.user.entity;

import com.team3.otboo.domain.base.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user")
@Getter
@NoArgsConstructor
public class User extends BaseEntity {
    @Column(length = 50, nullable = false, unique = true)
    String username;
    @Column(length = 50, nullable = false, unique = true)
    String password;
    @Column(length = 50, nullable = false, unique = true)
    String email;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    Role role;
    Boolean isLocked;
    LocalDateTime lastLoginAt;

//    LocalDateTime tempPasswordIssuedAt;
//    LocalDateTime tempPasswordExpiresAt;
    String profileImageUrl;

    public User(String username, String password, String email, Role role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
        this.isLocked = false;
        this.lastLoginAt = LocalDateTime.now();
    }
}
