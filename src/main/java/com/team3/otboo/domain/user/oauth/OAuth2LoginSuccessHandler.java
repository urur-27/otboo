package com.team3.otboo.domain.user.oauth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team3.otboo.domain.user.dto.AccessRefreshToken;
import com.team3.otboo.domain.user.jwt.CookieUtil;
import com.team3.otboo.domain.user.jwt.JwtService;
import com.team3.otboo.domain.user.user_details.CustomUserDetails;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

// OAuth2 로그인 성공 시 호출되는 핸들러
// 소셜 로그인 사용자에게 토큰 발급 위함

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;

    @Value("${oauth.frontend-redirect-url}")
    private String frontendRedirectUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        log.info("OAuth2 Login success!!");

        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        jwtService.invalidateJwtSession(userDetails.getUser().getId());
        log.info("이전에 세션이 존재하고 있다면 만료 완료");
        AccessRefreshToken tokens = jwtService.registerJwtSession(userDetails.getUser());
        log.info("token 두개 생성 완료");

        CookieUtil.setRefreshTokenCookie(response, tokens.refreshToken());

        // 프론트 코드의 url와 맞추기 위해
        String targetUrl = createRedirectUrl(frontendRedirectUrl);

        response.sendRedirect(targetUrl);
        log.info("redirectUrl: {}", targetUrl);
    }

    private String createRedirectUrl(String baseUrl){
        return UriComponentsBuilder.fromUriString(baseUrl + "/")
                .build()
                .encode(StandardCharsets.UTF_8)
                .toUriString();
    }
}
