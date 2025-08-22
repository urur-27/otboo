package com.team3.otboo.domain.user.jwt;

import com.team3.otboo.domain.user.entity.User;

import java.time.Instant;

public record JwtObject(
        Instant issueTime,
        Instant expirationTime,
        User user,
        String token
) {
    // 만료 시 true
    public boolean isExpired() {
        return expirationTime.isBefore(Instant.now());
    }
}
