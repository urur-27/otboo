package com.team3.otboo.domain.clothing.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.team3.otboo.domain.clothing.dto.response.CursorPageResponse;
import com.team3.otboo.domain.clothing.entity.QAttribute;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import com.team3.otboo.domain.clothing.entity.Attribute;



@Repository
@RequiredArgsConstructor
public class AttributeRepositoryImpl implements AttributeRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public CursorPageResponse<Attribute> findAllByCursor(
            String cursor,
            int limit,
            String sortBy,
            Sort.Direction direction,
            String keyword
    ) {
        // QueryDSL 시작
        QAttribute attribute = QAttribute.attribute;

        List<Attribute> results = queryFactory
                .selectFrom(attribute)
                .where( // null 조건은 자동 제외
                        cursorCondition(attribute, cursor, direction), // 커서
                        keyword != null ? attribute.name.containsIgnoreCase(keyword) : null // 키워드
                )
                .orderBy(getSortOrder(attribute, sortBy, direction))
                .limit(limit + 1)
                .fetch();

        boolean hasNext = results.size() > limit;
        List<Attribute> content = hasNext ? results.subList(0, limit) : results;
        String nextCursor = hasNext ? content.get(content.size() - 1).getId().toString() : null;

        Long totalCount = queryFactory
                .select(attribute.count())
                .from(attribute)
                .fetchOne();

        return new CursorPageResponse<>(
                content,
                nextCursor,
                sortBy,
                direction.name(),
                totalCount
        );
    }

    private BooleanExpression cursorCondition(QAttribute attribute, String cursor, Sort.Direction direction) {
        if (cursor == null) return null;
        UUID cursorId = UUID.fromString(cursor);
        // 정렬 방향에 따른 조회 조건 생성
        return direction.isAscending()
                ? attribute.id.gt(cursorId)
                : attribute.id.lt(cursorId);
    }

    private OrderSpecifier<?> getSortOrder(QAttribute attribute, String sortBy, Sort.Direction direction) {
//        PathBuilder<Attribute> entityPath = new PathBuilder<>(Attribute.class, "attribute");

        if (sortBy == null || sortBy.equals("createdAt")) {
            return direction.isAscending() ? attribute.createdAt.asc() : attribute.createdAt.desc();
        } else if (sortBy.equals("name")) {
            return direction.isAscending() ? attribute.name.asc() : attribute.name.desc();
        } else {
            return direction.isAscending() ? attribute.id.asc() : attribute.id.desc();
        }
    }
}
