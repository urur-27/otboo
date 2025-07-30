package com.team3.otboo.domain.user.dto;

public record CsrfToken(
        String token,
        String parameterName,
        String headerName
) {
}
