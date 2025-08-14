package com.team3.otboo.domain.user.controller;

import com.team3.otboo.domain.user.dto.Request.*;
import com.team3.otboo.domain.user.dto.response.UserDtoCursorResponse;
import com.team3.otboo.domain.user.dto.response.UserResponse;
import com.team3.otboo.domain.user.dto.Request.UserCreateRequest;
import com.team3.otboo.domain.user.dto.Request.UserLockUpdateRequest;
import com.team3.otboo.domain.user.dto.Request.UserRoleUpdateRequest;
import com.team3.otboo.domain.user.service.CustomUserDetailsService;
import com.team3.otboo.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    // 회원가입 -> 권한 상관없이 누구나 호출 가능
    @PostMapping
    public ResponseEntity<UserResponse> signUp(@RequestBody UserCreateRequest userCreateRequest) {
        UserResponse userDto = userService.createUser(userCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
    }

    @PatchMapping("/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateUserRole(
            @PathVariable("userId") UUID userId,
            @RequestBody UserRoleUpdateRequest userRoleUpdateRequest
    ){
        UserResponse response = userService.updateUserRole(userRoleUpdateRequest, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{userId}/password")
    public ResponseEntity<String> updatePassword(
            @PathVariable("userId") UUID userId,
            @RequestBody ChangePasswordRequest changePasswordRequest
    ) {
        log.info("비밀번호 변경요청");
        String newPassword = changePasswordRequest.password();
        userService.changeUserPassword(userId, newPassword);
        return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다. 다시 로그인해주세요.");
    }

    @PatchMapping("/{userId}/lock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UUID> updateLock(
            @PathVariable("userId") UUID userId,
            @RequestBody UserLockUpdateRequest userLockUpdateRequest
    ){
        UUID userUpdateId = userService.updateUserLock(userLockUpdateRequest, userId);

        return ResponseEntity.ok(userUpdateId);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDtoCursorResponse> listUsers(
            @Validated @ModelAttribute UserSearchParams params
    ) {
        return ResponseEntity.ok(userService.getUsers(params));
    }
}
