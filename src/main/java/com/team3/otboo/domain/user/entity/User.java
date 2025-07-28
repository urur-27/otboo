package com.team3.otboo.domain.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import java.time.LocalDateTime;

@Table(name = "user")
@Getter
public class User {
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
    LocalDateTime tempPasswordIssuedAt;
    LocalDateTime tempPasswordExpiresAt;

    String profileImageUrl;
}
