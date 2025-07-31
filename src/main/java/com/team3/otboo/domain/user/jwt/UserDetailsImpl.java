package com.team3.otboo.domain.user.jwt;

import com.team3.otboo.domain.user.dto.UserDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Getter
@RequiredArgsConstructor
// Spring Security가 사용할 custom user 정보 객체
public class UserDetailsImpl implements UserDetails {

    private final UserDto userDto;
    private final String password;

    // 사용자의 권한/역할 정보 return (ADMIN, USER 반환)
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(userDto.role().name()));
    }

    @Override
    public String getPassword() {
        return password;
    }
    @Override
    public String getUsername() {
        return userDto.name();
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof UserDetailsImpl that)) return false;
        return userDto.name().equals(that.userDto.name());
    }

    @Override
    public int hashCode() {
        return Objects.hash(userDto.name());
    }
}
