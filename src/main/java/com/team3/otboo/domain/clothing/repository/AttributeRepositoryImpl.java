package com.team3.otboo.domain.clothing.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.team3.otboo.domain.clothing.dto.response.CursorPageResponse;
import com.team3.otboo.domain.clothing.entity.QAttribute;
import com.team3.otboo.domain.clothing.service.Direction;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import com.team3.otboo.domain.clothing.entity.Attribute;



@Repository
@RequiredArgsConstructor
public class AttributeRepositoryImpl implements AttributeRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public CursorPageResponse<Attribute> findAllByCursor(
            String cursor,
            UUID idAfter,
            int limit,
            String sortBy,
            Direction direction,
            String keyword
    ) {
        // QueryDSL 시작
        QAttribute attribute = QAttribute.attribute;

        BooleanExpression keywordCond = (keyword != null) ? attribute.name.containsIgnoreCase(keyword) : null;
        BooleanExpression cursorCond = cursorCondition(attribute, cursor, idAfter, sortBy, direction);

        List<Attribute> results = queryFactory
                .selectFrom(attribute)
                .where(cursorCond, keywordCond)
                .orderBy(getSortOrder(attribute, sortBy, direction), getIdSortOrder(attribute, direction))
                .limit(limit + 1)
                .fetch();

        boolean hasNext = results.size() > limit;
        List<Attribute> content = hasNext ? results.subList(0, limit) : results;
        String nextCursor = hasNext
                ? getSortValue(content.getLast(), sortBy)
                : null;
        UUID nextIdAfter = hasNext ? content.getLast().getId() : null;


        Long totalCount = queryFactory
                .select(attribute.count())
                .from(attribute)
                .where(keywordCond)
                .fetchOne();

        return new CursorPageResponse<>(
                content,
                nextCursor,
                nextIdAfter,
                sortBy,
                direction.name(),
                totalCount,
                hasNext
        );
    }

    private BooleanExpression cursorCondition(
            QAttribute attribute, String cursor, UUID idAfter, String sortBy, Direction direction
    ) {
        if (cursor == null) return null;

        // 정렬 기준별 커서 값 파싱
        if (sortBy == null || sortBy.equals("createdAt")) {
            Instant cursorCreatedAt = Instant.parse(cursor);
            if (idAfter == null) {
                return direction.isAscending()
                        ? attribute.createdAt.gt(cursorCreatedAt)
                        : attribute.createdAt.lt(cursorCreatedAt);
            } else {
                if (direction.isAscending()) {
                    return attribute.createdAt.gt(cursorCreatedAt)
                            .or(attribute.createdAt.eq(cursorCreatedAt).and(attribute.id.gt(idAfter)));
                } else {
                    return attribute.createdAt.lt(cursorCreatedAt)
                            .or(attribute.createdAt.eq(cursorCreatedAt).and(attribute.id.lt(idAfter)));
                }
            }
        } else if (sortBy.equals("name")) {
            if (idAfter == null) {
                return direction.isAscending()
                        ? attribute.name.gt(cursor)
                        : attribute.name.lt(cursor);
            } else {
                if (direction.isAscending()) {
                    return attribute.name.gt(cursor)
                            .or(attribute.name.eq(cursor).and(attribute.id.gt(idAfter)));
                } else {
                    return attribute.name.lt(cursor)
                            .or(attribute.name.eq(cursor).and(attribute.id.lt(idAfter)));
                }
            }
        } else {
            // id 단독 정렬 등 기타 케이스
            UUID cursorId = UUID.fromString(cursor);
            return direction.isAscending()
                    ? attribute.id.gt(cursorId)
                    : attribute.id.lt(cursorId);
        }
    }

    private OrderSpecifier<?> getSortOrder(QAttribute attribute, String sortBy, Direction direction) {
//        PathBuilder<Attribute> entityPath = new PathBuilder<>(Attribute.class, "attribute");

        if (sortBy == null || sortBy.equals("createdAt")) {
            return direction.isAscending() ? attribute.createdAt.asc() : attribute.createdAt.desc();
        } else if (sortBy.equals("name")) {
            return direction.isAscending() ? attribute.name.asc() : attribute.name.desc();
        } else {
            return direction.isAscending() ? attribute.id.asc() : attribute.id.desc();
        }
    }

    private OrderSpecifier<?> getIdSortOrder(QAttribute attribute, Direction direction) {
        return direction.isAscending() ? attribute.id.asc() : attribute.id.desc();
    }

    private String getSortValue(Attribute attr, String sortBy) {
        if (sortBy == null || sortBy.equals("createdAt")) {
            return attr.getCreatedAt().toString();
        } else if (sortBy.equals("name")) {
            return attr.getName();
        } else {
            return attr.getId().toString();
        }
    }
}
