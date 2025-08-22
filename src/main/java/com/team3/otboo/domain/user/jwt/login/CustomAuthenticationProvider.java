package com.team3.otboo.domain.user.jwt.login;

import com.team3.otboo.domain.user.user_details.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j

@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = authentication.getName();
        String password = authentication.getCredentials().toString();

        CustomUserDetails userDetails
                = (CustomUserDetails) userDetailsService.loadUserByUsername(email);

        String tempPassword = userDetails.getTempPassword();
        LocalDateTime expiration = userDetails.getTempPasswordExpirationDate();

        // 임시 비밀번호가 없는 경우 -> manager(daoprovider)
        if(tempPassword == null || tempPassword.isEmpty()) {
            log.info("임시비밀번호로 로그인 불가 확인 및 daoprovider 호출");
            return null;
        }

        if (passwordEncoder.matches(password, tempPassword)) {
            // 임시 비밀번호가 일치하면, 만료 시간을 확인
            if (expiration != null && expiration.isAfter(LocalDateTime.now())) {
                // 만료되지 않았다면, 임시 권한을 부여하여 로그인 성공 처리
                // 프론트엔드에서 이 권한을 보고 비밀번호 변경 페이지로 리다이렉트 할 수 있음
                return new UsernamePasswordAuthenticationToken(
                        userDetails, null, List.of(new SimpleGrantedAuthority("ROLE_TEMP_USER"))
                );
            } else {
                throw new CredentialsExpiredException("임시 비밀번호가 만료되었습니다.");
            }
        } else {
            throw new BadCredentialsException("임시 비밀번호가 일치하지 않습니다.");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
