package com.team3.otboo.domain.user.service;

import com.team3.otboo.domain.user.dto.UserDto;
import com.team3.otboo.domain.user.entity.Profile;
import com.team3.otboo.domain.user.entity.User;
import com.team3.otboo.domain.user.enums.Role;
import com.team3.otboo.domain.user.mapper.UserMapper;
import com.team3.otboo.domain.user.repository.ProfileRepository;
import com.team3.otboo.domain.user.repository.UserRepository;
import com.team3.otboo.global.exception.BusinessException;
import com.team3.otboo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Random;

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
    private final ProfileRepository profileRepository;
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
        else {
            String encodedPassword = passwordEncoder.encode(password);
            User user = new User(username, email, encodedPassword, Role.ADMIN, null);
            log.info("Admin 계정이 만들어졌습니다.");
            userRepository.save(user);

            Profile profile = Profile.of(null, null, null, null, null, user);
            user.setProfile(profile);
            profileRepository.save(profile);

            return userMapper.toDto(user);
        }
    }

    @Override
    @Transactional
    public String setTempPassword(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "해당 이메일을 가진 사용자가 없습니다.")
        );
        log.info("이메일로 사용자를 찾음:{}", email);

        // random password
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        String tempPassword = Base64.getUrlEncoder().withoutPadding().encodeToString(salt);

        // 임시 password 저장
        user.setTempPassword(passwordEncoder.encode(tempPassword));
        return tempPassword;
    }
}
