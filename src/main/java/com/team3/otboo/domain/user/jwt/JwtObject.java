package com.team3.otboo.domain.user.jwt;

import com.team3.otboo.domain.user.dto.UserDto;
import com.team3.otboo.domain.user.entity.User;

import java.time.Instant;

// 정보를 담아놓은 객체
// (발급 시간, 만료 시간, 사용자 정보(User), 토큰)
public record JwtObject(
        // 발급 시간
        Instant issueTime,
        // 만료 시간
        Instant expirationTime,
        User user,
        String token
) {
    // 만료 시 true
    public boolean isExpired() {
        return expirationTime.isBefore(Instant.now());
    }
}
