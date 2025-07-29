package com.team3.otboo.domain.user.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team3.otboo.domain.user.jwt.SecurityMatchers;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configurers.userdetails.DaoAuthenticationConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

//    public SecurityFilterChain securityFilterChain(
//            HttpSecurity http,
//            ObjectMapper mapper,
//            DaoAuthenticationProvider daoAuthenticationProvider,
//            SessionRegistry sessionRegistry,
//            PersistentTokenBasedRememberMeServices rememberMeServices
//    ) throws Exception {
//        // 인증 -> DaoAuthenticationProvider
//        // 인가 -> 일부 URL은 허용, 나머지는 ADMIN 권한 필요
//        // CSRF -> 기본 활성화, 로그아웃은 예외처리
//        // 로그아웃 -> 세션 삭제 + 200 응답
//        // 로그인 필터 -> 기본 필터 대신 JSON기반 필터 사용
//        // 세션 관리 -> 세션 고정 공격 방지 + 동시 로그인 제한
//        // Rememberme -> JDBC 기반 토큰 저장으로 자동 로그인 지원
//        http
//                .authenticationProvider(daoAuthenticationProvider)
//                .authorizeHttpRequests(authorize -> authorize
//                        .requestMatchers(
//                                SecurityMatchers.GET_CSRF_TOKEN,
//                                SecurityMatchers.SIGN_UP
//                        ))
//                .csrf()
//    }

    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
