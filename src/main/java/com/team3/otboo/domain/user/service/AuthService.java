package com.team3.otboo.domain.user.service;

import com.team3.otboo.domain.user.dto.Request.UserLockUpdateRequest;
import com.team3.otboo.domain.user.dto.Request.UserRoleUpdateRequest;
import com.team3.otboo.domain.user.dto.UserDto;
import com.team3.otboo.domain.user.enums.Role;

import java.util.UUID;


public interface AuthService {
    UserDto initAdmin();
    UserDto updateRole(UUID userId, Role role);
    UserDto updateLock(UUID userId, Boolean locked);
}
