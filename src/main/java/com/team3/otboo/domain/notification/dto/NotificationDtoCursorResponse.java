package com.team3.otboo.domain.notification.dto;

import com.team3.otboo.domain.notification.enums.SortDirection;
import java.util.List;
import java.util.UUID;

public record NotificationDtoCursorResponse(
    List<NotificationDto> data,
    String nextCursor,
    UUID nexIdAfter,
    boolean hasNext,
    long totalCount,
    String sortBy,
    SortDirection sortDirection
) {
}
