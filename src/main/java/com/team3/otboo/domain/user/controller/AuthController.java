package com.team3.otboo.domain.user.controller;

import com.team3.otboo.domain.user.dto.AccessRefreshToken;
import com.team3.otboo.domain.user.dto.Request.ChangePasswordRequest;
import com.team3.otboo.domain.user.dto.Request.ResetPasswordRequest;
import com.team3.otboo.domain.user.dto.UserDto;
import com.team3.otboo.domain.user.jwt.JwtService;
import com.team3.otboo.domain.user.jwt.JwtSession;
import com.team3.otboo.domain.user.service.AuthService;
import com.team3.otboo.domain.user.service.CustomUserDetailsService;
import com.team3.otboo.domain.user.service.EmailService;
import com.team3.otboo.domain.user.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Slf4j
@RequiredArgsConstructor
public class AuthController {

    private final JwtService jwtService;
    private final AuthService authService;
    private final EmailService emailService;

    // 로그인, 로그아웃 기능 자동 처리됨.

    // 임시 비밀번호 발급해주는 api
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest resetPasswordRequest) {
        log.info("임시 비밀번호 발급");
        String tempPassword = authService.setTempPassword(resetPasswordRequest.email());

        log.info("이메일로 임시 비밀번호 전송");
        emailService.sendTempPasswordResetEmail(resetPasswordRequest.email(), tempPassword);
        return ResponseEntity.ok("입력하신 이메일로 임시 비밀번호가 발송되었습니다.");
    }

    @PostMapping("/refresh")
    public ResponseEntity<String> refresh(
            @CookieValue(name = "refresh_token") String refreshToken,
            HttpServletResponse response
    ) {
        log.info("토큰 재발급 요청");
        AccessRefreshToken accessRefreshToken = jwtService.refreshJwtSession(refreshToken);

        Cookie refreshTokenCookie = new Cookie(JwtService.REFRESH_TOKEN_COOKIE_NAME,
                accessRefreshToken.refreshToken());
        refreshTokenCookie.setHttpOnly(true);
        response.addCookie(refreshTokenCookie);

        return ResponseEntity.ok(accessRefreshToken.accessToken());
    }

    // access token 새로 생성
    // session에 따로 저장해놓지 않았기 때문
    @GetMapping("/me")
    public ResponseEntity<String> me(
            @CookieValue(name = "refresh_token") String refreshToken
    ) {
        log.info("Access token 조회 시작");
        AccessRefreshToken accessRefreshToken = jwtService.meJwtRefreshToken(refreshToken);
        log.info("Access token 조회 완료");
        return ResponseEntity.status(HttpStatus.OK).body(accessRefreshToken.accessToken());
    }

    // 자동으로 token, parameterName, headerName이 포함되어있음
    @GetMapping("/csrf-token")
    public ResponseEntity<CsrfToken> getCsrfToken(CsrfToken csrfToken) {
        log.debug("CSRF 토큰 요청");
        return ResponseEntity.status(HttpStatus.OK).body(csrfToken);
    }
}
