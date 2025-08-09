package com.team3.otboo.domain.user.dto;

public record AccessRefreshToken(
        String accessToken,
        String refreshToken
) {
}
