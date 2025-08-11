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

@Slf4j
@Configuration
@EnableWebSecurity // spring security 활성화
@EnableMethodSecurity // (Secured, PreAuthorize) 보안 어노테이션 활성화
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            ObjectMapper objectMapper,
            DaoAuthenticationProvider daoAuthenticationProvider,
            JwtService jwtService
    ) throws Exception {
        // 인증 -> DaoAuthenticationProvider
        // 인가 -> 일부 URL은 허용, 나머지는 ADMIN 권한 필요
        // CSRF -> 기본 활성화, 로그아웃은 예외처리
        // 로그아웃 -> 세션 삭제 + 200 응답
        // 로그인 필터 -> 기본 필터 대신 JSON기반 필터 사용
        // 세션 관리 -> 세션 고정 공격 방지 + 동시 로그인 제한
        http
                // 사용자 인증 처리 provider 등록
                // 직접 설정한 dao~ 를 통해 사용자 인증을 수행
                .authenticationProvider(daoAuthenticationProvider)
                // 인가 정책 설정
                .authorizeHttpRequests(authorize -> authorize
                        //.requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/csrf-token").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/refresh").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/sign-in").permitAll()
                        .requestMatchers("/uploads/**").permitAll() // 로컬 이미지 찾기 위한 url 경로 허용
                        .requestMatchers("/api/**").authenticated()

                        .requestMatchers("/", "/assets/**", "/**.html", "/**.css", "/**.js", "/favicon.ico").permitAll()
                        .anyRequest().authenticated()
                )
                // .csrf(csrf -> csrf.disable())
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
                //
                .logout(logout ->
                        logout
                                // logout URL 경로 지정
                                .logoutUrl("/api/auth/sign-out")
                                // 로그아웃 성공 시 리다이렉트 대신 HTTP 200 OK 상태 코드만 반환
                                .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler())
                                // JWT 무효화하는 로직 수행
                                .addLogoutHandler(new JwtLogoutHandler(jwtService))
                )
                // 세션 관리
                .sessionManagement(session ->
                        session
                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // JWT 인증 필터 추가
                // 로그인 이후의 모든 요청에 대해 헤더의 JWT를 검증하고 인증 상태를 설정하는 필터
                // 사용자 이름, 비밀번호 인증 필터보다 먼저 실행되어야 한다.
                .addFilterBefore(new JwtAuthenticationFilter(jwtService, objectMapper),
                        JsonUsernamePasswordAuthenticationFilter.class);
//                // Form 로그인 및 HTTP Basic 인증 비활성화
//                .formLogin(form -> form.disable())
//                .httpBasic(basic -> basic.disable());
        // HttpSecurity 설정 후 SecurityFilterChain 반환
        return http.build();
    }

    // 사용자가 입력한 원문 비밀번호를 안전하게 저장하고 검증하기 위해 해싱 알고리즘 적용
    @Bean
    public PasswordEncoder passwordEncoder() {
        // Bcrypt는 내부에 salt를 자동으로 생성하여 해시값을 계산하고,
        // 반복(rounds)처리로 연산 비용을 조절할 수 있어서 (brute-force)무차별 대입 공격에 강함
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(
            // DB에서 사용자의 아이디, 해시된 비밀번호, 권한 목록을 조회해 UserDetails객체로 반환하는 서비스
            UserDetailsService userDetailsService,
            // 로그인 시 입력된 비밀번호와 저장된 해시를 비교할 때 사용
            PasswordEncoder passwordEncoder,
            // RoleHierarchy 정보를 기반으로 작동(ADMIN 권한을 가진 사용자는 자동으로 USER 권한도 갖도록 매핑해준다)
            RoleHierarchy roleHierarchy
    ) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        // 사용자 정보 조회 서비스
        provider.setUserDetailsService(userDetailsService);
        // 비밀번호 검증 로직
        provider.setPasswordEncoder(passwordEncoder);
        // 권한 계층 매핑 (ADMIN -> USER) (상속)
        provider.setAuthoritiesMapper(new RoleHierarchyAuthoritiesMapper(roleHierarchy));
        return provider;
    }

    // 권한 계층
    // 여러 권한 간의 상속 관계를 정의
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
