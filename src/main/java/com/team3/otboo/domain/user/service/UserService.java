package com.team3.otboo.domain.user.service;

import com.team3.otboo.domain.user.dto.*;
import com.team3.otboo.domain.user.dto.Request.UserCreateRequest;
import com.team3.otboo.domain.user.dto.Request.UserPasswordUpdateRequest;
import com.team3.otboo.domain.user.dto.Request.UserRoleUpdateRequest;
import com.team3.otboo.domain.user.dto.response.UserResponse;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserResponse createUser(UserCreateRequest request);
    UserDto findUserById(UUID id);
    List<UserDto> findAll();
    UserDtoCursorResponse getUsers(UserSearchCondition condition);
    void updateUserLock(UserLockUpdateRequest request);
    UserResponse updateUserRole(UserRoleUpdateRequest request, UUID id);
    void updateUserPassword(UserPasswordUpdateRequest request, UUID id);
    void deleteUser(UUID id);
}
