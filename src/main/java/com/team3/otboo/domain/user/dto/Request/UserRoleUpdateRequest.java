package com.team3.otboo.domain.user.dto.Request;

import com.team3.otboo.domain.user.enums.Role;

import java.util.UUID;

public record UserRoleUpdateRequest(
        Role newRole
) {
}
