package com.team3.otboo.domain.user.controller;

import com.team3.otboo.domain.user.dto.Request.*;
import com.team3.otboo.domain.user.dto.response.UserDtoCursorResponse;
import com.team3.otboo.domain.user.dto.response.UserResponse;
import com.team3.otboo.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
        UserResponse userDto = userService.updateUserRole(userRoleUpdateRequest, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
    }

    @PatchMapping("/{userId}/password")
    public ResponseEntity<Void> updatePassword(
            @PathVariable("userId") UUID userId,
            @RequestBody UserPasswordUpdateRequest userRoleUpdateRequest
    ){
        userService.updateUserPassword(userRoleUpdateRequest, userId);

        return ResponseEntity.noContent().build();
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
