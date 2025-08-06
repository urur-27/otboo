package com.team3.otboo.domain.user.jwt;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import java.util.Arrays;
import java.util.Optional;

/*
    사용자가 로그아웃을 요청할 때, 현재 요청의 Access Token을 무효화한다.
    요청 헹더에서 Access Token을 꺼내 블랙리스트에 추가
    서버에 저장된 refresh token도 함께 삭제한다.

    사용자와 서버 양쪽 모두에서 토큰의 흔적을 지우는 핸들러
*/

@RequiredArgsConstructor
public class JwtLogoutHandler implements LogoutHandler {

    private final JwtService jwtService;

    @Override
    @SneakyThrows
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        // Refresh token을 요청이 포함된 쿠키에서 찾는다
        resolveRefreshToken(request)
                // Refresh token이 존재할 경우
                .ifPresent(refreshToken -> {
                    // DB에 저장된 세션 정보 삭제 및 블랙리스트 추가
                    jwtService.invalidateJwtSession(refreshToken);
                    // 사용자의 브라우저에서 refresh token 쿠키를 즉시 만료시킨다.
                    invalidateRefreshTokenCookie(response);
                });
    }

    // HttpServletRequest에 포함된 쿠키 배열을 스트림을 처리
    // 지정된 이름을 가진 쿠키의 값을 찾아서 Optional로 반환
    private Optional<String> resolveRefreshToken(HttpServletRequest request) {
        if(request.getCookies() == null) {
            return Optional.empty();
        }

        // 사용자가 가진 모든 쿠키들을 살펴보는 과정
        return Arrays.stream(request.getCookies())
                // 쿠키 이름이 Refresh token 쿠키 이름과 일치하는 것만 필터링
                .filter(cookie -> cookie.getName().equals(JwtService.REFRESH_TOKEN_COOKIE_NAME))
                // 필터링된 쿠키 중 첫번째꺼
                .findFirst()
                // 찾은 쿠키 객체에서 실제 토큰 값만 추출
                .map(Cookie::getValue);
    }

    // 사용자에게 Refresh token 쿠키를 삭제하라는 응답을 보낸다 -> 브라우저로 보낸다고 생각해도 무방
    // 브라우저의 쿠키를 직접 서버가 삭제할 수 없으므로 아래의 메서드는 브라우저에게 보내는 삭제요청서 역할
    // 새로운 쿠키를 만들어서 기존 쿠키를 대체하게 한 후
    // 쿠키의 maxAge를 0으로 설정하면 브라우저는 해당 쿠키를 즉시 삭제
    private void invalidateRefreshTokenCookie(HttpServletResponse response) {
        // 삭제할 쿠키와 동일한 이름으로 새 쿠키를 생성 -> 값은 비워둔다
        Cookie refreshTokenCookie = new Cookie(JwtService.REFRESH_TOKEN_COOKIE_NAME, "");
        // 쿠키의 유효 시간을 0으로 설정하여 즉시 만료
        refreshTokenCookie.setMaxAge(0);
        // 보안을 위해 HttpOnly 속성 유지
        refreshTokenCookie.setHttpOnly(true);
        // 응답에 쿠키를 추가하여 사용자에게 전달
        response.addCookie(refreshTokenCookie);
    }
}
