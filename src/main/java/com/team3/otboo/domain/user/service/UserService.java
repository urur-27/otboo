package com.team3.otboo.domain.user.service;

import com.team3.otboo.domain.user.dto.*;
import com.team3.otboo.domain.user.dto.Request.UserCreateRequest;
import com.team3.otboo.domain.user.dto.Request.UserLockUpdateRequest;
import com.team3.otboo.domain.user.dto.Request.UserRoleUpdateRequest;
import com.team3.otboo.domain.user.dto.response.UserCreateResponse;
import com.team3.otboo.domain.user.dto.response.UserDtoCursorResponse;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserCreateResponse createUser(UserCreateRequest request);
    UserDto findUserById(UUID id);
    List<UserDto> findAll();
    UserDtoCursorResponse getUsers(UserSearchCondition condition);
    void deleteUser(UUID id);
}
