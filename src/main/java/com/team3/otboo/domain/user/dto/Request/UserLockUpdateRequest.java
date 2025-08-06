package com.team3.otboo.domain.user.dto.Request;

import java.util.UUID;

public record UserLockUpdateRequest(
        boolean locked
) {
}
