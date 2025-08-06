package com.team3.otboo.domain.user.jwt;

import com.team3.otboo.domain.user.dto.UserDto;

import java.time.Instant;

// 정보를 담아놓은 객체
// (발급 시간, 만료 시간, 사용자 정보(UserDto), 토큰)
public record JwtObject(
        // 발급 시간
        Instant issueTime,
        // 만료 시간
        Instant expirationTime,
        UserDto userDto,
        String token
) {
    // 만료 여부 확인하는 메서드
    public boolean isExpired() {
        return Instant.now().isBefore(expirationTime);
    }
}
