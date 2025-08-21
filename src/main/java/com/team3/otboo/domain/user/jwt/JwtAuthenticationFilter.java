package com.team3.otboo.domain.user.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team3.otboo.domain.user.entity.User;
import com.team3.otboo.domain.user.user_details.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private record PublicEndpoint(String path, String method) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PublicEndpoint that = (PublicEndpoint) o;
            return Objects.equals(path, that.path) && Objects.equals(method, that.method);
        }

        @Override
        public int hashCode() {
            return Objects.hash(path, method);
        }
    }

    // 인증을 건너뛸 경로와 메소드 목록을 Set으로 정의합니다.
    private static final Set<PublicEndpoint> PUBLIC_ENDPOINTS = Set.of(
            new PublicEndpoint("/api/auth/sign-in", "POST"),
            new PublicEndpoint("/api/users", "POST"),
            new PublicEndpoint("/api/auth/reset-password", "POST"),
            new PublicEndpoint("/api/auth/refresh", "POST"),
            new PublicEndpoint("/api/auth/csrf-token", "GET")
    );

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    // 모든 사용자의 요청이 들어올 때마다 한 번씩 실행되며, 다음과 같은 순서로 동작한다.
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {
        // request에서 access token을 꺼낸다.
        Optional<String> optionalAccessToken = resolveAccessToken(request);
        // 모두에게 허용되지 않는 api요청이고, 존재한다면
        if (optionalAccessToken.isPresent() && !isPermitAll(request)) {
            String accessToken = optionalAccessToken.get();

            // 토큰 검증 성공 시, 인증된 사용자의 요청으로 처리
            if (jwtService.validate(accessToken)) {
                User user = jwtService.parse(accessToken).user();
                CustomUserDetails userDetails = new CustomUserDetails(user);
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );
                SecurityContextHolder.getContext().setAuthentication(auth);

                chain.doFilter(request, response);

            } else {
                // jwtService.invalidateJwtSession(accessToken);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding("UTF-8");
                // error처리 추가 필요
                response.getWriter().write(objectMapper.writeValueAsString(HttpServletResponse.SC_UNAUTHORIZED));
            }
        } else {
            chain.doFilter(request, response);
        }
    }

    // Request의 Authorization Header에서 JWT 문자열만 추출하여 반환하는 메서드
    private Optional<String> resolveAccessToken(HttpServletRequest request) {
        String prefix = "Bearer ";
        return Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION))
                .map(value -> {
                    if (value.startsWith(prefix)) {
                        return value.substring(prefix.length());
                    } else {
                        return null;
                    }
                });
    }

    private boolean isPermitAll(HttpServletRequest request) {
        PublicEndpoint currentRequest = new PublicEndpoint(request.getRequestURI(), request.getMethod());
        return PUBLIC_ENDPOINTS.contains(currentRequest);
    }
}