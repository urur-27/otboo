package com.team3.otboo.domain.user.service;

import com.team3.otboo.domain.user.dto.*;
import com.team3.otboo.domain.user.dto.Request.UserCreateRequest;
import com.team3.otboo.domain.user.dto.response.UserCreateResponse;
import com.team3.otboo.domain.user.entity.Profile;
import com.team3.otboo.domain.user.entity.User;
import com.team3.otboo.domain.user.enums.Role;
import com.team3.otboo.domain.user.mapper.UserMapper;
import com.team3.otboo.domain.user.repository.ProfileRepository;
import com.team3.otboo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    // 회원가입
    @Override
    @Transactional
    public UserCreateResponse createUser(UserCreateRequest request) {
        log.debug("사용자 생성 시작: {}", request.name());

        if(userRepository.existsByEmail(request.email())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
        }
        if(userRepository.existsByUsername(request.name())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already exists");
        }

        String encodedPassword = passwordEncoder.encode(request.password());

        User user = new User(request.name(), request.email(), encodedPassword, Role.USER, null);
        userRepository.save(user);

        Profile profile = Profile.of(null, null, null, null, null, user);
        user.setProfile(profile);

        profileRepository.save(profile);

        log.info("사용자 생성 완료: {}", user.getUsername());

        return UserCreateResponse.of(user);
    }

    @Override
    public UserDto findUserById(UUID id){
        log.debug("사용자 조회 시작: {}", id);
        UserDto userDto = userRepository.findById(id)
                .map(userMapper::toDto)
                .orElseThrow(
                        ()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
                );
        log.info("사용자 조회 완료: {}", userDto.name());
        return userDto;
    }

    @Override
    public List<UserDto> findAll() {
        log.debug("전체 사용자 조회 시작");
        List<User> users = userRepository.findAll();
        log.info("전체 사용자 조회 완료");
        return users.stream().map(userMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public UserDtoCursorResponse getUsers(UserSearchCondition condition){
        // todo: 음....
        return null;
    }

    @Override
    public void updateUserLock(UserLockUpdateRequest request){
        // todo: 사용자 계정을 잠글 경우 사용
    }

    @Override
    public void updateUserRole(UserRoleUpdateRequest request){
        // todo: 사용자의 권한을 변경할 때 사용, 변경 시 자동 로그아웃
    }

    @Override
    public void deleteUser(UUID id) {
        log.debug("사용자 삭제 시작: {}", id);
        if(!userRepository.existsById(id)){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        userRepository.deleteById(id);
        log.info("사용자 삭제 완료");
    }
}