package com.team3.otboo.domain.user.user_details;

import com.team3.otboo.domain.user.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.LocalDateTime;
import java.util.*;

@Getter
public class CustomUserDetails implements UserDetails, OAuth2User {

    private final User user;
    private final Map<String, Object> attributes;

    // 정상 로그인 용
    public CustomUserDetails(User user){
        this.user = user;
        this.attributes = Collections.emptyMap();
    }

    // 소셜 로그인 용
    public CustomUserDetails(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    public UUID getId() {
        return user.getId();
    }

    public String getTempPassword() {
        return user.getTempPassword();
    }

    public LocalDateTime getTempPasswordExpirationDate() {
        return user.getTempPasswordExpirationDate();
    }

    // override하여 메서드 명은 getUsername이지만, 실상 구현은 getEmail 역할
    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    // 계정 만료 여부 (현재는 모든 계정이 만료되지 않음)
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // 계정 잠금 여부
    @Override
    public boolean isAccountNonLocked() {
        return !user.isLocked();
    }

    // 자격 증명 만료 여부 (현재는 모든 자격 증명이 만료되지 않음)
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // OAuth2User Method
    // 소셜 로그인인 경우 attributes map, 아닌 경우 null 또는 빈 map을 반환
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    // todo: 당장은 null을 반환하도록 설정
    // google(sub), kakao(id)로 지정되어있는 사용자를 식별하는 고유한 key값을 반환하는 메서드
    @Override
    public String getName() {
        return user.getEmail();
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        CustomUserDetails that = (CustomUserDetails) o;
        return Objects.equals(user.getId(), that.user.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(user.getId());
    }
}