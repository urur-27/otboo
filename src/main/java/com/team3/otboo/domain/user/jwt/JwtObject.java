package com.team3.otboo.domain.user.jwt;

import com.team3.otboo.domain.user.dto.UserDto;

import java.time.Instant;

public record JwtObject(
        // 발급 시간
        Instant issueTime,
        // 만료 시간
        Instant expirationTime,
        UserDto userDto,
        String token
) {
}
