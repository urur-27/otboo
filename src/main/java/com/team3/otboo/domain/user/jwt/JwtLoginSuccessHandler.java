package com.team3.otboo.domain.user.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team3.otboo.domain.user.dto.AccessRefreshToken;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

/*
    로그인 인증 성공 후의 로직 처리 핸들러
    새로운 JWT 세션(Access, Refresh token) 을 생성하고 사용자에게 전달
 */

@RequiredArgsConstructor
public class JwtLoginSuccessHandler implements AuthenticationSuccessHandler {
    // 인증에 성공한 사용자 정보를 바탕으로 createToken을 호출
    // Access, Refresh Token 생성

    private final ObjectMapper objectMapper;
    private final JwtService jwtService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        // 인증된 사용자 정보를 가져온다
        UserDetailsImpl user = (UserDetailsImpl) authentication.getPrincipal();
        // 기존에 이미 발급된 사용자의 JWT 세션이 있다면 모두 무효화처리
        // 동시 로그인 제한 및 이전에 사용하던 기기에서의 세션 만료 역할
        jwtService.invalidateJwtSession(user.getUserDto().id());
        // 새로운 token 2개 생성 및 Refresh token DB에 저장
        AccessRefreshToken token = jwtService.registerJwtSession(user.getUserDto());

        // Refresh Token은 보안을 위해 HttpOnly cookie에 담아 사용자에게 전달한다.
        String refreshToken = token.refreshToken();
        Cookie refreshTokenCookie = new Cookie(JwtService.REFRESH_TOKEN_COOKIE_NAME, refreshToken);
        // javascript에서 접근할 수 없도록 설정
        refreshTokenCookie.setHttpOnly(true);
        // Https 환경에서만 쿠키가 전송되도록 설정 -> 보안을 위해
        refreshTokenCookie.setSecure(true);
        // Refresh token의 만료 시간과 동일하게 설정
        refreshTokenCookie.setMaxAge(2592000);

        // AccessToken은 사용자가 API 요청 시마다 사용해야하므로, Responsebody에 JSON형태로 담아 전달한다.
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // Access Token 문자열만 응답 본문에 작성
        response.getWriter().write(objectMapper.writeValueAsString(token.accessToken()));
    }
}
