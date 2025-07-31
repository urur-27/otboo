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
// 서버가 Refresh Token을 추적하고 관리하기 위해 사용하는 DB table
// JWT의 Stateless 특성을 보완하기 위함
// 토큰 재발급, 강제 로그아웃 기능 구현을 위함
public class JwtSession extends BaseEntity {

    @Column(updatable = false, nullable = false)
    private UUID userId;
    @Column(nullable = false, unique = true)
    private String accessToken;
    @Column(nullable = false, unique = true)
    private String refreshToken;
    @Column(nullable = false)
    private Instant expirationTime;

    // 현재 Access token이 만료되었는지
    public boolean isExpired() {
        return this.expirationTime.isBefore(Instant.now());
    }

    // token 재발급 시 새로운 토큰 정보로 세션 업데이트
    public void update(UUID userId, String accessToken, String refreshToken, Instant expirationTime) {
        this.userId = userId;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expirationTime = expirationTime;
    }
}
