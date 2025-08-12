package com.team3.otboo.domain.notification.dto;

import com.team3.otboo.domain.notification.enums.SortDirection;
import java.util.UUID;

public record NotificationSearchCondition(
    String cursor,
    UUID isAfter,
    int limit,
    String sortBy,
    SortDirection sortDirection
) {

  public NotificationSearchCondition {
    if (limit <= 0) {
      limit = 10;
    }
    if (sortBy == null || sortBy.isBlank()) {
      sortBy = "createdAt";
    }
    if (sortDirection == null) {
      sortDirection = SortDirection.DESC;
    }
  }
}
