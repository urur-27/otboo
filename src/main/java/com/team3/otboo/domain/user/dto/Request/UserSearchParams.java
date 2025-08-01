package com.team3.otboo.domain.user.dto.Request;

import com.team3.otboo.domain.user.enums.Role;
import com.team3.otboo.domain.user.enums.SortBy;
import com.team3.otboo.domain.user.enums.SortDirection;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

public record UserSearchParams(
        // 페이지네이션
        @RequestParam(required = false) String cursor,
        @RequestParam(name = "idAfter", required = false) UUID idAfter,
        @RequestParam(name = "limit", required = true)       int    limit,
        // 정렬
        @RequestParam(name = "sortBy",        required = true) SortBy sortBy,
        @RequestParam(name = "sortDirection", required = true) SortDirection sortDirection,
        // 필터
        @RequestParam(required = false) String emailLike,
        @RequestParam(required = false) Role roleEqual,
        @RequestParam(required = false) Boolean locked
) {}
