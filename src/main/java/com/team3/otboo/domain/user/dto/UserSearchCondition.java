package com.team3.otboo.domain.user.dto;

public record UserSearchCondition(
        String cursor,
        String idAfter,
        int limit,
        String sortBy,
        String sortDirection,
        String emailLike,
        String roleEqual,
        Boolean locked
) {
}
