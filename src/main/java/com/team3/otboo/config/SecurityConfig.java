package com.team3.otboo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team3.otboo.domain.user.enums.Role;
import com.team3.otboo.domain.user.jwt.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyAuthoritiesMapper;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

import java.util.List;

@Slf4j
@Configuration
@EnableWebSecurity // spring security 활성화
@EnableMethodSecurity // (Secured, PreAuthorize) 보안 어노테이션 활성화
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            ObjectMapper objectMapper,
            AuthenticationManager authenticationManager,
            JwtService jwtService
    ) throws Exception {
        // 인증 -> DaoAuthenticationProvider
        // 인가 -> 일부 URL은 허용, 나머지는 ADMIN 권한 필요
        // CSRF -> 기본 활성화, 로그아웃은 예외처리
        // 로그아웃 -> 세션 삭제 + 200 응답
        // 로그인 필터 -> 기본 필터 대신 JSON기반 필터 사용
        // 세션 관리 -> 세션 고정 공격 방지 + 동시 로그인 제한
        http
                // dao~, custom provider를 가지고 있는 manager
                .authenticationManager(authenticationManager)
                // 인가 정책 설정
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/csrf-token").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/refresh").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/sign-in").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/reset-password").permitAll()
                        //.requestMatchers(HttpMethod.PATCH, "/api/auth/change-password").hasRole("TEMP_USER") // 임시 비밀번호를 발급받은 사용자만 접근 가능
                        .requestMatchers("/uploads/**").permitAll() // 로컬 이미지 찾기 위한 url 경로 허용
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll() // 헬스체크 허용
                        .requestMatchers(HttpMethod.GET, "/api/clothes/extractions").permitAll() // 임시 허용.
                        .requestMatchers("/api/**").authenticated()

                        .requestMatchers("/", "/assets/**", "/**.html", "/**.css", "/**.js", "/favicon.ico").permitAll()
                        .anyRequest().authenticated()
                )
                .csrf(csrf ->
                        csrf
                                // 로그아웃 요청에 대해 CSRF 보호를 비활성화 -> 빠른 처리를 위해
                                .ignoringRequestMatchers("/api/auth/sign-out")
                                // 서버에서 생성한 CSRF 토큰을 쿠키에 저장하여 사용자에게 전달, JS에서 접근할 수 있게 함
                                // XSRF-TOKEN, X-XSRF-TOKEN 자동 지정해줌
                                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                                // CSRF 토큰을 요청(request) 속성에서도 사용할 수 있도록 설정
                                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                                // CSRF 보호 기능이 불필요하게 세션을 생성하는 것을 방지
                                .sessionAuthenticationStrategy(new NullAuthenticatedSessionStrategy())
                )
                .with(
                        new JsonUsernamePasswordAuthenticationFilter.Configurer(objectMapper),
                        configurer ->
                                configurer
                                        .successHandler(new JwtLoginSuccessHandler(objectMapper, jwtService))
                                        .failureHandler(new CustomLoginFailureHandler(objectMapper))
                )
                .logout(logout ->
                        logout
                                // logout URL 경로 지정
                                .logoutUrl("/api/auth/sign-out")
                                // 로그아웃 성공 시 리다이렉트 대신 HTTP 200 OK 상태 코드만 반환
                                .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler())
                                // JWT 무효화하는 로직 수행
                                .addLogoutHandler(new JwtLogoutHandler(jwtService))
                )
                .sessionManagement(session ->
                        session
                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // JWT 인증 필터 추가
                .addFilterBefore(new JwtAuthenticationFilter(jwtService, objectMapper),
                        JsonUsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder,
            RoleHierarchy roleHierarchy
    ) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        provider.setAuthoritiesMapper(new RoleHierarchyAuthoritiesMapper(roleHierarchy));
        return provider;
    }

    @Bean
    public CustomAuthenticationProvider customAuthenticationProvider(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder
    ) {
        return new CustomAuthenticationProvider(userDetailsService, passwordEncoder);
    }

    // AuthenticationManager에 모든 Provider들을 등록
    @Bean
    public AuthenticationManager authenticationManager(
            CustomAuthenticationProvider customAuthenticationProvider,
            DaoAuthenticationProvider daoAuthenticationProvider
    ) {
        // 사용하는 모든 Provider들을 리스트 형태로 전달
        return new ProviderManager(List.of(customAuthenticationProvider, daoAuthenticationProvider));
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        // ADMIN를 USER가 상속하도록 설정한다.
        // 그래서 ADMIN 권한을 가지고 있으면 hasRole("USER") 체크를 통과한다.
        return RoleHierarchyImpl.withDefaultRolePrefix()
                .role(Role.ADMIN.name())
                .implies(Role.USER.name())
                .build();
    }
}
