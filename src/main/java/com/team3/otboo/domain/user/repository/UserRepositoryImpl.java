package com.team3.otboo.domain.user.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.team3.otboo.domain.user.entity.QUser;
import com.team3.otboo.domain.user.entity.User;
import com.team3.otboo.domain.user.enums.Role;
import com.team3.otboo.domain.user.enums.SortBy;
import com.team3.otboo.domain.user.enums.SortDirection;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<User> findByFilterUser(
            String         emailCursor,
            UUID           idAfter,
            int            limit,
            SortBy         sortBy,
            SortDirection  sortDirection,
            String         emailLike,
            Role           roleEqual,
            Boolean        locked
    ) {
        return queryFactory
                .selectFrom(QUser.user)
                .where(
                        afterPredicate(emailCursor, idAfter, sortDirection),
                        emailContains(emailLike),
                        roleEq(roleEqual),
                        lockedEq(locked)
                )
                .orderBy(
                        sortDirection.isAscending()
                                ? QUser.user.email.asc()
                                : QUser.user.email.desc(),
                        sortDirection.isAscending()
                                ? QUser.user.id.asc()
                                : QUser.user.id.desc()
                )
                .limit(limit)
                .fetch();
    }

    @Override
    public Long totalCount(String emailLike, Role roleEqual, Boolean locked) {
        return queryFactory
                .selectFrom(QUser.user)
                .where(
                        emailContains(emailLike),
                        roleEq(roleEqual),
                        lockedEq(locked)
                )
                .fetchCount();
    }

    private BooleanExpression afterPredicate(
            String        emailCursor,
            UUID          idAfter,
            SortDirection dir
    ) {
        if (emailCursor == null || emailCursor.isBlank()) {
            return null;
        }

        // 기본 커서 비교 (이메일)
        BooleanExpression primary = dir.isAscending()
                ? QUser.user.email.gt(emailCursor)
                : QUser.user.email.lt(emailCursor);

        // tiebreaker: 이메일 같을 때 ID 순서
        if (idAfter != null) {
            BooleanExpression tieBreaker = QUser.user.email.eq(emailCursor)
                    .and(dir.isAscending()
                            ? QUser.user.id.gt(idAfter)
                            : QUser.user.id.lt(idAfter));
            return primary.or(tieBreaker);
        }

        return primary;
    }

    // 이메일 부분 검색
    private BooleanExpression emailContains(String emailLike) {
        return (emailLike != null && !emailLike.isBlank())
                ? QUser.user.email.containsIgnoreCase(emailLike)
                : null;
    }

    // 역할 일치
    private BooleanExpression roleEq(Role roleEqual) {
        return roleEqual != null
                ? QUser.user.role.eq(roleEqual)
                : null;
    }

    // 잠금 여부 일치
    private BooleanExpression lockedEq(Boolean locked) {
        return locked != null
                ? QUser.user.locked.eq(locked)
                : null;
    }
}
