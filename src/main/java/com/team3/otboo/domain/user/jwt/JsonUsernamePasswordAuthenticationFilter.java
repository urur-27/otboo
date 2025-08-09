package com.team3.otboo.domain.user.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team3.otboo.domain.user.dto.Request.SignInRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractAuthenticationFilterConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.util.matcher.RequestMatcher;

// spring security에서 JSON 기반의 로그인 인증을 처리하기 위해 만든 클래스
// 부모클래스는 HTML Form을 통해 전송되는 것을 처리한다.

@RequiredArgsConstructor
public class JsonUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final ObjectMapper objectMapper;

    // 실제 인증을 시도하는 메서드
    // sign-in 요청이 오면 이 메서드 호출
    // -> 요청 본문 (JSON)에서 이메일과 비밀번호를 추출
    // -> UsernamePasswordAuthenticationToken을 생성
    // -> 인증을 AuthenticationManager에게 위임한다.
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) throws AuthenticationException {
        // 로그인 요청이 POST여야한다.
        if (!request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException(
                    "Authentication method not supported: " + request.getMethod());
        }
        try {
            // JSON 요청 본문 -> 로그인 요청 DTO
            SignInRequest signInRequest = objectMapper.readValue(request.getInputStream(),
                    SignInRequest.class);
            // 요청 본문으로부터 인증에 필요한 토큰 객체 생성
            UsernamePasswordAuthenticationToken authRequest =
                    new UsernamePasswordAuthenticationToken(signInRequest.email(), signInRequest.password());
            setDetails(request, authRequest);
            // 인증 관리자(Authentication Provider)에게 인증에 필요한 토큰 객체와 함께 인증 위임
            // DaoAuthenticationProvider가 처리할 예정 (여러 provider중 아이디, 비번으로 인증 처리하는데에는 dao가 제격)
            return this.getAuthenticationManager().authenticate(authRequest);
        } catch (IOException e) {
            throw new AuthenticationServiceException("Request parsing failed", e);
        }
    }

    // JsonUsernamePasswordAuthenticationFilter를 초기화하고 설정하는 역할
    // 이 필터가 어떤 URL, HTTP method에 대해 동작할지,
    // 인증 성공 및 실패 시 어떤 핸들러를 사용할지 등을 설정 후 필터 객체 반환
    public static JsonUsernamePasswordAuthenticationFilter createDefault(
            ObjectMapper objectMapper,
            AuthenticationManager authenticationManager,
            SessionAuthenticationStrategy sessionAuthenticationStrategy,
            RememberMeServices rememberMeServices
    ) {
        JsonUsernamePasswordAuthenticationFilter filter = new JsonUsernamePasswordAuthenticationFilter(
                objectMapper);
        // 로그인에 대해서만 필터 작동
        filter.setRequiresAuthenticationRequestMatcher(
                // request 객체를 받아 URI와 Method를 직접 비교합니다.
                request -> "/api/auth/sign-in".equals(request.getRequestURI())
                        && "POST".equals(request.getMethod())
        );
        filter.setAuthenticationManager(authenticationManager);
        filter.setAuthenticationSuccessHandler(new CustomLoginSuccessHandler(objectMapper));
        filter.setAuthenticationFailureHandler(new CustomLoginFailureHandler(objectMapper));
        filter.setSecurityContextRepository(new HttpSessionSecurityContextRepository());
        filter.setSessionAuthenticationStrategy(sessionAuthenticationStrategy);
        filter.setRememberMeServices(rememberMeServices);
        return filter;
    }

    // HttpSercurity 설정에 이 JSON 커스텀 필터를 쉽게 추가할 수 있도록 도와주는 클래스
    public static class Configurer extends
            AbstractAuthenticationFilterConfigurer<HttpSecurity, Configurer, JsonUsernamePasswordAuthenticationFilter> {

        public Configurer(ObjectMapper objectMapper) {
            super(new JsonUsernamePasswordAuthenticationFilter(objectMapper), "/api/auth/sign-in");
        }

        @Override
        protected RequestMatcher createLoginProcessingUrlMatcher(String loginProcessingUrl) {
            // 필터가 어떤 요청을 처리할 지 정확히 지정하는 역할 (RequestMatcher 생성)
            return request -> loginProcessingUrl.equals(request.getRequestURI())
                    && HttpMethod.POST.name().equals(request.getMethod());
        }

        @Override
        public void init(HttpSecurity http) throws Exception {
            loginProcessingUrl("/api/auth/sign-in");
        }
    }
}