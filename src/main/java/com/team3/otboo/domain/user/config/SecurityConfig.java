package com.team3.otboo.domain.user.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Slf4j
@Configuration
@EnableWebSecurity // spring security 활성화
@EnableMethodSecurity // (Secured, PreAuthorize) 보안 어노테이션 활성화
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, ObjectMapper mapper) throws Exception {
        // 인증 -> DaoAuthenticationProvider
        // 인가 -> 일부 URL은 허용, 나머지는 ADMIN 권한 필요
        // CSRF -> 기본 활성화, 로그아웃은 예외처리
        // 로그아웃 -> 세션 삭제 + 200 응답
        // 로그인 필터 -> 기본 필터 대신 JSON기반 필터 사용
        // 세션 관리 -> 세션 고정 공격 방지 + 동시 로그인 제한
        // Rememberme -> JDBC 기반 토큰 저장으로 자동 로그인 지원
        http
                // 사용자 인증 처리 provider 등록
                // 직접 설정한 dao~ 를 통해 사용자 인증을 수행
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                // 인가 정책 설정
                .authorizeHttpRequests(authorize -> authorize

                        .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/csrf-token").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/me").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/refresh").permitAll()

                        .anyRequest().authenticated()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
