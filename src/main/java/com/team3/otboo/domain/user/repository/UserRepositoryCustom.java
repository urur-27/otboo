package com.team3.otboo.domain.user.repository;

import com.team3.otboo.domain.user.entity.User;
import com.team3.otboo.domain.user.enums.Role;
import com.team3.otboo.domain.user.enums.SortBy;
import com.team3.otboo.domain.user.enums.SortDirection;

import java.util.List;
import java.util.UUID;

public interface UserRepositoryCustom {
    List<User> findByFilterUser(
            String       emailCursor,   // 이전 페이지의 마지막 이메일
            UUID idAfter,       // 동일 이메일 내 tiebreaker 용 마지막 ID
            int          limit,
            SortBy sortBy,        // 이제 사실상 사용되지 않음(항상 EMAIL)
            SortDirection sortDirection,
            String       emailLike,
            Role roleEqual,
            Boolean      locked
    );

    Long totalCount(String emailLike, Role roleEqual, Boolean locked);
}
