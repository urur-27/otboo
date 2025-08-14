package com.team3.otboo.domain.user.jwt;

import com.team3.otboo.domain.base.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Table(name = "jwt_sessions")
@NoArgsConstructor
@AllArgsConstructor
public class JwtSession extends BaseEntity {

    @Column(updatable = false, nullable = false)
    private UUID userId;
    // 기본길이 255
    @Column(nullable = false, unique = true, length = 1024)
    private String refreshToken;
    @Column(nullable = false)
    private Instant expirationTime;

    // 현재 Refresh token이 만료되었는지
    public boolean isExpired() {
        return this.expirationTime.isBefore(Instant.now());
    }

    public void update(UUID userId, String refreshToken, Instant expirationTime) {
        this.userId = userId;
        this.refreshToken = refreshToken;
        this.expirationTime = expirationTime;
    }
}
