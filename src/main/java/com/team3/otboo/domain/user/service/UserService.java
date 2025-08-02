package com.team3.otboo.domain.user.service;

import com.team3.otboo.domain.user.dto.*;
import com.team3.otboo.domain.user.dto.Request.*;
import com.team3.otboo.domain.user.dto.response.UserDtoCursorResponse;
import com.team3.otboo.domain.user.dto.response.UserResponse;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserResponse createUser(UserCreateRequest request);
    UserDto findUserById(UUID id);
    List<UserDto> findAll();
    UserDtoCursorResponse getUsers(UserSearchParams userSearchParams);
    UUID updateUserLock(UserLockUpdateRequest request, UUID id);
    UserResponse updateUserRole(UserRoleUpdateRequest request, UUID id);
    void updateUserPassword(UserPasswordUpdateRequest request, UUID id);
    void deleteUser(UUID id);
}
