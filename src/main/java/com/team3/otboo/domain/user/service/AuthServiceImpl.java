package com.team3.otboo.domain.user.service;

import com.team3.otboo.domain.user.dto.UserDto;
import com.team3.otboo.domain.user.entity.User;
import com.team3.otboo.domain.user.enums.Role;
import com.team3.otboo.domain.user.mapper.UserMapper;
import com.team3.otboo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
