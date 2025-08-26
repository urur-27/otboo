package com.team3.otboo.domain.user.service;

import com.team3.otboo.domain.user.dto.*;
import com.team3.otboo.domain.user.dto.Request.*;
import com.team3.otboo.domain.user.dto.response.UserDtoCursorResponse;
import com.team3.otboo.domain.user.dto.response.UserResponse;
import com.team3.otboo.domain.user.dto.Request.UserCreateRequest;
import com.team3.otboo.domain.user.dto.Request.UserLockUpdateRequest;
import com.team3.otboo.domain.user.entity.Profile;
import com.team3.otboo.domain.user.entity.User;
import com.team3.otboo.domain.user.enums.Role;
import com.team3.otboo.domain.user.enums.SortBy;
import com.team3.otboo.domain.user.jwt.JwtService;
import com.team3.otboo.domain.user.mapper.UserMapper;
import com.team3.otboo.domain.user.repository.ProfileRepository;
import com.team3.otboo.domain.user.repository.UserRepository;
import com.team3.otboo.global.exception.BusinessException;
import com.team3.otboo.global.exception.ErrorCode;
import com.team3.otboo.global.exception.user.RoleNotFoundException;
import com.team3.otboo.global.exception.user.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    // 회원가입
    @Override
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        log.debug("사용자 생성 시작: {}", request.name());

        if(userRepository.existsByEmail(request.email())){
            throw new BusinessException(ErrorCode.ALREADY_EXISTS, "이미 가입된 이메일입니다.");
        }
        if(userRepository.existsByUsername(request.name())){
            throw new BusinessException(ErrorCode.ALREADY_EXISTS, "이미 가입한 사용자 이름입니다.");
        }

        String encodedPassword = passwordEncoder.encode(request.password());

        User user = new User(request.name(), request.email(), encodedPassword, Role.USER, null);
        userRepository.save(user);

        Profile profile = Profile.of(null, null, null, null, null, user);
        user.setProfile(profile);

        profileRepository.save(profile);

        log.info("사용자 생성 완료: {}", user.getUsername());

        return UserResponse.of(user);
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
    public UserDtoCursorResponse getUsers(UserSearchParams userSearchParams){
        List<User> users = userRepository.findByFilterUser(
                userSearchParams.cursor(),
                userSearchParams.idAfter(), userSearchParams.limit() + 1,
                userSearchParams.sortBy(), userSearchParams.sortDirection(),
                userSearchParams.emailLike(), userSearchParams.roleEqual(), userSearchParams.locked()
        );

        boolean hasNext = getHasNext(users, userSearchParams.limit());
        UUID  nextIdAfter = getNextIdAfter(users, userSearchParams.limit());
        String nextCursor  = getNextCursor(users, userSearchParams.limit(), userSearchParams.sortBy());

        Long totalCount = userRepository.totalCount(userSearchParams.emailLike(), userSearchParams.roleEqual(), userSearchParams.locked());

        List<User> page = hasNext ? users.subList(0, userSearchParams.limit()) : users;
        List<UserDto> userDtos = page.stream().map(userMapper::toDto).collect(Collectors.toList());

        return UserDtoCursorResponse.of(userDtos, nextCursor, nextIdAfter, hasNext, totalCount, userSearchParams.sortBy(), userSearchParams.sortDirection());
    }

    @Override
    @Transactional
    public UUID updateUserLock(UserLockUpdateRequest request, UUID userId){
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        user.updateLocked(request.locked());

        // 계정을 잠금 상태로 변경할 경우, 토큰 무효화 진행
        if(request.locked()) {
            jwtService.logout(user.getId());
        }
        return user.getId();
    }

    @Override
    @Transactional
    public UserResponse updateUserRole(UserRoleUpdateRequest request, UUID userId){
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        log.info("user role: {}, request role: {}", user.getRole(), request.role());
        if(!Role.contains(request.role())){
            throw new RoleNotFoundException();
        }

        user.updateRole(request.role());

        // 권한 변경 후 해당 사용자가 로그인한 세션 만료 처리
        jwtService.logout(user.getId());
        return UserResponse.of(user);
    }

    @Override
    @Transactional
    public void changeUserPassword(UUID userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        String encodedPassword = passwordEncoder.encode(newPassword);
        user.changePassword(encodedPassword);
    }

    public static boolean getHasNext(List<?> items, int limit) {
        return items.size() > limit;
    }

    public static UUID getNextIdAfter(List<User> users, int limit) {
        if (!getHasNext(users, limit)) {
            return null;
        }
        return users.get(limit - 1).getId();
    }

    public static String getNextCursor(List<User> users, int limit, SortBy sortBy){
        if (!getHasNext(users, limit)) {
            return null;
        }
        User next = users.get(limit - 1);
        if (SortBy.email.equals(sortBy)) {
            return URLEncoder.encode(next.getEmail(), StandardCharsets.UTF_8);
        } else {
            return next.getId().toString();
        }
    }
}