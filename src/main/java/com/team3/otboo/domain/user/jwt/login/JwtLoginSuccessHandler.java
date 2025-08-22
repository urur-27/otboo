package com.team3.otboo.domain.user.jwt.login;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team3.otboo.domain.user.dto.AccessRefreshToken;
import com.team3.otboo.domain.user.user_details.CustomUserDetails;
import com.team3.otboo.domain.user.jwt.CookieUtil;
import com.team3.otboo.domain.user.jwt.JwtService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/*
    로그인 인증 성공 후의 로직 처리 핸들러
    새로운 JWT 세션(Access, Refresh token) 을 생성하고 사용자에게 전달
 */
@Slf4j
@Component
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
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        // 기존에 이미 발급된 사용자의 JWT 세션이 있다면 모두 무효화처리
        // 동시 로그인 제한 및 이전에 사용하던 기기에서의 세션 만료 역할
        jwtService.invalidateJwtSession(user.getUser().getId());
        log.info("이전에 세션이 존재하고 있다면 만료 완료");

        // 새로운 token 2개 생성 및 Refresh token DB에 저장
        AccessRefreshToken token = jwtService.registerJwtSession(user.getUser());
        log.info("token 두개 생성 완료");

        // Refresh Token은 보안을 위해 HttpOnly cookie에 담아 사용자에게 전달한다.
        CookieUtil.setRefreshTokenCookie(response, token.refreshToken());

        // AccessToken은 사용자가 API 요청 시마다 사용해야하므로, Responsebody에 JSON형태로 담아 전달한다.
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // Access Token 문자열만 응답 본문에 작성
        response.getWriter().write(objectMapper.writeValueAsString(token.accessToken()));
    }
}
