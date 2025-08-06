package com.team3.otboo.domain.user.controller;

import com.team3.otboo.domain.user.dto.UserDto;
import com.team3.otboo.domain.user.jwt.JwtService;
import com.team3.otboo.domain.user.jwt.JwtSession;
import com.team3.otboo.domain.user.service.AuthService;
import com.team3.otboo.domain.user.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Slf4j
@RequiredArgsConstructor
public class AuthController {

    private final JwtService jwtService;
    private final AuthService authService;

    // 로그인 기능 자동 처리됨.

    @PostMapping("/sign-out")
    public ResponseEntity<UserDto> signOut() {
        log.info("로그아웃 시작");

        log.info("로그아웃 완료");
        return null;
    }

    @PostMapping("reset-password")
    public ResponseEntity<UserDto> resetPassword(@RequestBody String email) {
        log.info("비밀번호 초기화 시작");

        log.info("비밀번호 초기화 완료");
        return null;
    }

    @PostMapping("/refresh")
    public ResponseEntity<String> refresh(
            @RequestParam String refresh_token
    ) {
        log.info("토큰 재발급 요청");
        JwtSession jwtSession = jwtService.refreshJwtSession(refreshToken);

        Cookie refreshTokenCookie = new Cookie(JwtService.REFRESH_TOKEN_COOKIE_NAME,
                jwtSession.getRefreshToken());
        refreshTokenCookie.setHttpOnly(true);
        response.addCookie(refreshTokenCookie);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(jwtSession.getAccessToken())
                ;
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> me(@RequestParam String refreshToken) {
        log.info("Access token 조회 시작");

        log.info("Access token 조회 완료");
        return null;
    }

    @GetMapping("/csrf-token")
    public ResponseEntity<UserDto> csrfToken(HttpServletRequest request) {
        log.info("CSRF token 조회 시작");
        // request에서 csrf token을 가져온다
        CsrfToken csrfToken = (CsrfToken) request.getAttribute("_csrf");


        log.info("CSRF token 조회 완료");
        return null;
    }
}
