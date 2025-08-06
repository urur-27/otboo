package com.team3.otboo.domain.user.service;

import com.team3.otboo.domain.user.entity.User;
import com.team3.otboo.domain.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.findByUsername(username).orElseThrow(
			() -> new EntityNotFoundException("user not found. username: " + username));

		return new CustomUserDetails(user);
	}

	/**
	 * Spring Security의 UserDetails 인터페이스를 구현한 커스텀 클래스 User 엔티티를 Spring Security가 이해할 수 있는 형태로
	 * 변환한다.
	 */
	public static class CustomUserDetails implements UserDetails {

		private final User user;

		public CustomUserDetails(User user) {
			this.user = user;
		}

		@Override
		public Collection<? extends GrantedAuthority> getAuthorities() {
			// 사용자의 역할을 Spring Security 권한으로 변환
			return Collections.singletonList(
				new SimpleGrantedAuthority("ROLE_USER"));
		}

		public UUID getId() {
			return user.getId();
		}

		@Override
		public String getPassword() {
			return user.getPassword();
		}

		@Override
		public String getUsername() {
			return user.getUsername();
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

		// User 엔티티에 접근할 수 있는 메서드
		public User getUser() {
			return user;
		}
	}
}
