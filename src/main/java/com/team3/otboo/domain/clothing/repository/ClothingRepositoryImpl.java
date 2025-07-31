package com.team3.otboo.domain.clothing.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.team3.otboo.domain.clothing.dto.response.CursorPageResponse;
import com.team3.otboo.domain.clothing.entity.Clothing;
import com.team3.otboo.domain.clothing.entity.QClothing;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ClothingRepositoryImpl implements ClothingRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public CursorPageResponse<Clothing> findAllByCursor(
            UUID ownerId,
            String cursor,
            UUID idAfter,
            int limit,
            String typeEqual,
            Sort.Direction direction
    ) {
        QClothing clothing = QClothing.clothing;

        BooleanExpression ownerCond = clothing.owner.id.eq(ownerId);
        BooleanExpression typeCond = (typeEqual != null) ? clothing.type.eq(typeEqual) : null;
        BooleanExpression cursorCond = cursorCondition(clothing, cursor, idAfter, direction);

        List<Clothing> results = queryFactory
                .selectFrom(clothing)
                .where(ownerCond, typeCond, cursorCond)
                .orderBy(getSortOrder(clothing, direction), getIdSortOrder(clothing, direction))
                .limit(limit + 1)
                .fetch();

        boolean hasNext = results.size() > limit;
        List<Clothing> content = hasNext ? results.subList(0, limit) : results;
        String nextCursor = hasNext ? content.getLast().getCreatedAt().toString() : null; // createdAt 커서
        UUID nextIdAfter = hasNext ? content.getLast().getId() : null;

        Long totalCount = queryFactory
                .select(clothing.count())
                .from(clothing)
                .where(ownerCond, typeCond)
                .fetchOne();

        return new CursorPageResponse<>(
                content,
                nextCursor,
                nextIdAfter,
                "createdAt",
                direction.name(),
                totalCount,
                hasNext
        );
    }

    private BooleanExpression cursorCondition(
            QClothing clothing,
            String cursor,   // createdAt 커서
            UUID idAfter,    // 보조 커서
            Sort.Direction direction
    ) {
        if (cursor == null) return null;

        Instant cursorCreatedAt = Instant.parse(cursor);

        if (idAfter == null) {
            // 단순히 createdAt 기준으로만 커서 조건
            return direction.isAscending()
                    ? clothing.createdAt.gt(cursorCreatedAt)
                    : clothing.createdAt.lt(cursorCreatedAt);
        } else {
            // 복합 커서
            if (direction.isAscending()) {
                return clothing.createdAt.gt(cursorCreatedAt)
                        .or(clothing.createdAt.eq(cursorCreatedAt)
                                .and(clothing.id.gt(idAfter)));
            } else {
                return clothing.createdAt.lt(cursorCreatedAt)
                        .or(clothing.createdAt.eq(cursorCreatedAt)
                                .and(clothing.id.lt(idAfter)));
            }
        }
    }


    private OrderSpecifier<?> getSortOrder(QClothing clothing, Sort.Direction direction) {
        return direction.isAscending() ? clothing.createdAt.asc() : clothing.createdAt.desc();
    }

    // 보조 정렬
    private OrderSpecifier<?> getIdSortOrder(QClothing clothing, Sort.Direction direction) {
        return direction.isAscending() ? clothing.id.asc() : clothing.id.desc();
    }
}

