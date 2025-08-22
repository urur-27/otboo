package com.team3.otboo.domain.user.jwt;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public final class CookieUtil {

    private CookieUtil() {
    }

    private static long REFRESH_TOKEN_EXPIRATION_SECONDS;

    @Value("${security.jwt.refresh-token-validity-seconds}")
    private long refreshTokenSeconds;

    @PostConstruct
    public void init() {
        REFRESH_TOKEN_EXPIRATION_SECONDS = this.refreshTokenSeconds;
    }

    // 리프레시 토큰 쿠키를 생성하고 HttpServletResponse에 추가하는 메소드
    public static void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        // 쿠키에 대한 모든 설정은 이 메소드에 캡슐화
        ResponseCookie cookie = ResponseCookie.from(JwtService.REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                .path("/")
                .maxAge(REFRESH_TOKEN_EXPIRATION_SECONDS)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax") // CSRF 방어 관련 SameSite 설정
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }
}
