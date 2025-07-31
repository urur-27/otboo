package com.team3.otboo.domain.user.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team3.otboo.domain.user.enums.Role;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyAuthoritiesMapper;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.sql.DataSource;

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
            SessionRegistry sessionRegistry,
            PersistentTokenBasedRememberMeServices rememberMeServices
    ) throws Exception {
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
                        // permitAll -> 인증 없이 접근 허용
                        // 회원가입
                        .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
                        // csrf token을 get방식으로 요청하는 경우
                        .requestMatchers(HttpMethod.GET, "/api/auth/csrf-token").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/me").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/refresh").permitAll()

                        .anyRequest().authenticated()
                );
                // 기본적으로 활성화되지만, logout 요청은 csrf 토큰 없이도 요청할 수 있도록 예외처리
//                .csrf(csrf -> csrf
//                        // logout 패턴 지정
//                        .ignoringRequestMatchers("/api/auth/sign-out")
//                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
//                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
//                .logout(logout ->
//                        logout
//                                //
//                                .logoutRequestMatcher(config("api/auth/sign-out"))
//                                .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler())
//                                .addLogoutHandler(new SessionRegistry)
//                )

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

    // 세션 추적
    // 애플리케이션에 로그인한 모든 사용자 세션을 추적 관리
    // 동시 로그인 제어(maximumSession(1) 등),
    // 세션 만료 시 사용자 알림,
    // 관리 콘솔에 접속 중인 사용자 리스트 제공 등에 활용
//    @Bean
//    public SessionRegistry sessionRegistry() {
//        return new SessionRegistryImpl();
//    }

//    // RememberMe 자동 로그인
//    @Bean
//    public PersistentTokenBasedRememberMeServices rememberMeServices(
//            // 해시 계산 시 쓰이는 비밀 키.
//            @Value("${security.remember-me.key}") String key,
//            // 쿠키에 저장된 토큰의 만료기간(초)
//            @Value("${security.remember-me.token-validity-seconds}") int tokenValiditySeconds,
//            UserDetailsService userDetailsService,
//            DataSource dataSource
//    ) {
//        // DB 저장용 토큰 레포지토리
//        // 자동으로 persistent_logins 테이블 스키마를 기대하며
//        // 로그인 시 생성된 토큰을 DB에 CRUD한다.
//        JdbcTokenRepositoryImpl tokenRepository = new JdbcTokenRepositoryImpl();
//        tokenRepository.setDataSource(dataSource);
//
//        // remember-me 서비스 객체
//        PersistentTokenBasedRememberMeServices rememberMeServices = new PersistentTokenBasedRememberMeServices(
//                key, userDetailsService, tokenRepository);
//        rememberMeServices.setTokenValiditySeconds(tokenValiditySeconds);
//
//        return rememberMeServices;
//    }
}
