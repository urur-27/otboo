package com.team3.otboo.domain.user.service;

import com.team3.otboo.domain.user.dto.*;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserDto createUser(UserCreateRequest request);
    UserDto findUserById(UUID id);
    List<UserDto> findAll();
    UserDtoCursorResponse getUsers(UserSearchCondition condition);
    void updateUserLock(UserLockUpdateRequest request);
    void updateUserRole(UserRoleUpdateRequest request);
    void deleteUser(UUID id);
}
