package com.team3.otboo.domain.user.controller;

import com.team3.otboo.domain.user.dto.Request.UserCreateRequest;
import com.team3.otboo.domain.user.dto.Request.UserPasswordUpdateRequest;
import com.team3.otboo.domain.user.dto.Request.UserRoleUpdateRequest;
import com.team3.otboo.domain.user.dto.UserDtoCursorResponse;
import com.team3.otboo.domain.user.dto.UserSearchCondition;
import com.team3.otboo.domain.user.dto.response.UserResponse;
import com.team3.otboo.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    // 목록 조회
    // ADMIN 권한 필요
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDtoCursorResponse> getUsers(
            // 페이지네이션 파라미터
            @RequestParam(required = false) String cursor,
            @RequestParam(name = "idAfter", required = false) String idAfter,
            @RequestParam(name = "limit") int limit, // 필수 값

            // 정렬 파라미터
            @RequestParam(name = "sortBy") String sortBy,
            @RequestParam(name = "sortDirection") String sortDirection,

            // 필터링 파라미터
            @RequestParam(required = false) String emailLike,
            @RequestParam(required = false) String roleEqual,
            @RequestParam(required = false) Boolean locked
    ) {
        UserSearchCondition condition = new UserSearchCondition(
                cursor, idAfter, limit, sortBy, sortDirection, emailLike, roleEqual, locked
        );
        UserDtoCursorResponse response = userService.getUsers(condition);
        return ResponseEntity.ok(response);
     }
}
