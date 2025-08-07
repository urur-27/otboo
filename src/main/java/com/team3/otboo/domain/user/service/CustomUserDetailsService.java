package com.team3.otboo.domain.user.service;

import com.team3.otboo.domain.user.dto.UserDto;
import com.team3.otboo.domain.user.entity.User;
import com.team3.otboo.domain.user.mapper.UserMapper;
import com.team3.otboo.domain.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;

import java.util.*;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;
	private final UserMapper userMapper;

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		// username -> email(고유값)
		User user = userRepository.findByEmail(username).orElseThrow(
			() -> new EntityNotFoundException("user not found. username: " + username));

		return new CustomUserDetails(userMapper.toDto(user), user.getPassword());
	}

	/**
	 * Spring Security의 UserDetails 인터페이스를 구현한 커스텀 클래스 User 엔티티를 Spring Security가 이해할 수 있는 형태로
	 * 변환한다.
	 */
	@Getter
	@RequiredArgsConstructor
	public static class CustomUserDetails implements UserDetails {

		// private final User user;
		private final UserDto userDto;
		private final String password;

		@Override
		public Collection<? extends GrantedAuthority> getAuthorities() {
			return List.of(new SimpleGrantedAuthority(userDto.role().name()));
		}
		
		public UUID getId() {
			return userDto.id();
		}

		// override하여 메서드 명은 getUsername이지만, 실상 구현은 getEmail 역할
		@Override
		public String getUsername() {
			return userDto.email();
		}
		
		// 계정 만료 여부 (현재는 모든 계정이 만료되지 않음)
		@Override
		public boolean isAccountNonExpired() {
			return true;
		}

		// 계정 잠금 여부 (현재는 모든 계정이 잠기지 않음)
		@Override
		public boolean isAccountNonLocked() {
			return true;
		}

		// 자격 증명 만료 여부 (현재는 모든 자격 증명이 만료되지 않음)
		@Override
		public boolean isCredentialsNonExpired() {
			return true;
		}

		@Override
		public boolean equals(Object o) {
			if(this == o) return true;
			if(!(o instanceof CustomUserDetails that)) return false;
			return userDto.name().equals(that.userDto.name());
		}

		@Override
		public int hashCode() {
			return Objects.hash(userDto.name());
		}
	}
}
