package com.team3.otboo.domain.notification.repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.team3.otboo.domain.notification.dto.NotificationSearchCondition;
import com.team3.otboo.domain.notification.entity.Notification;
import com.team3.otboo.domain.notification.enums.SortDirection;
import com.team3.otboo.domain.user.entity.User;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import static com.team3.otboo.domain.notification.entity.QNotification.notification;

@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepositoryCustom{

  private final JPAQueryFactory queryFactory;

  @Override
  public List<Notification> findByReceiverWithCursor(User receiver,
      NotificationSearchCondition condition) {
    return queryFactory
        .selectFrom(notification)
        .where(
            notification.receiver.eq(receiver),
            cursorCondition(condition.cursor(), condition.isAfter(), condition.sortBy())
        )
        .orderBy(getOrderSpecifiers(condition.sortBy(), condition.sortDirection()))
        .limit(condition.limit() + 1)
        .fetch();
  }

  private OrderSpecifier<?>[] getOrderSpecifiers(String sortBy, SortDirection sortDirection) {
    Order order = (sortDirection == SortDirection.DESC) ? Order.DESC : Order.ASC;
    PathBuilder<Notification> entityPath = new PathBuilder<>(Notification.class, "notification");

    if ("title".equals(sortBy)) {
      return new OrderSpecifier[]{
          new OrderSpecifier<>(order, entityPath.get("title", String.class)),
          new OrderSpecifier<>(Order.DESC, entityPath.get("id", UUID.class))
      };
    }
    return new OrderSpecifier[]{new OrderSpecifier<>(order, notification.createdAt), new OrderSpecifier<>(Order.DESC, notification.id)};
  }

  private BooleanExpression cursorCondition(String cursor, UUID idAfter, String sortBy) {
    if (!StringUtils.hasText(cursor) || idAfter == null) {
      return null;
    }

    if ("title".equals(sortBy)) {
      return notification.title.lt(cursor)
          .or(notification.title.eq(cursor).and(notification.id.lt(idAfter)));
    }

    Instant createdAtCursor = Instant.parse(cursor);
    return notification.createdAt.lt(createdAtCursor)
        .or(notification.createdAt.eq(createdAtCursor).and(notification.id.lt(idAfter)));
  }
}
