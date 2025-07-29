package com.team3.otboo.domain.user.dto;

import java.util.List;

public record UserDtoCursorResponse(
        List<UserDto> data,
        String nextCursor,
        String nextIdAfter,
        boolean hasNext,
        long totalCount,
        String sortBy,
        String sortDirection
) {
}
