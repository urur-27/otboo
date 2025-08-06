package com.team3.otboo.domain.user.service;

import com.team3.otboo.domain.user.dto.Request.UserLockUpdateRequest;
import com.team3.otboo.domain.user.dto.Request.UserRoleUpdateRequest;
import com.team3.otboo.domain.user.dto.UserDto;
import com.team3.otboo.domain.user.entity.User;
import com.team3.otboo.domain.user.enums.Role;
import com.team3.otboo.domain.user.jwt.JwtService;
import com.team3.otboo.domain.user.jwt.JwtSessionRepository;
import com.team3.otboo.domain.user.mapper.UserMapper;
import com.team3.otboo.domain.user.repository.ProfileRepository;
import com.team3.otboo.domain.user.repository.UserRepository;
import com.team3.otboo.global.exception.user.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    @Value("${admin.username}")
    private String username;
    @Value("${admin.password}")
    private String password;
    @Value("${admin.email}")
    private String email;

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    @Transactional
    // 초기 관리자 세팅
    public UserDto initAdmin() {
        if(userRepository.existsByUsername(username) || userRepository.existsByEmail(email)) {
            log.warn("Admin 계정이 이미 존재합니다.");
            return null;
        }
        String encodedPassword = passwordEncoder.encode(password);
        User user = new User(username, email, encodedPassword, Role.ADMIN, null);
        userRepository.save(user);
        log.info("Admin 계정이 만들어졌습니다.");

        return userMapper.toDto(user);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    // 권한 수정 작업
    public UserDto updateRole(UUID userId, Role role) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException("User not found"));
        user.updateRole(role);

        // 권한 변경 후 해당 사용자가 로그인한 세션 만료 처리
        jwtService.invalidateJwtSession(user.getId());
        return userMapper.toDto(user);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    // 계정 잠금 상태 변경 작업 메서드
    public UserDto updateLock(UUID userId, Boolean locked) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException("User not found"));
        if(user.isLocked() == locked) {
            log.warn("이미 변경하고자 하는 잠금 상태임");
            return userMapper.toDto(user);
        }

        user.updateLocked(locked);

        // 계정을 잠금 상태로 변경할 경우, 토큰 무효화 진행
        if(locked) {
            jwtService.invalidateJwtSession(user.getId());
        }
        return userMapper.toDto(user);
    }
}
