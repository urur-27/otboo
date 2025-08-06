package com.team3.otboo.domain.clothing.dto.response;

import java.util.List;
import java.util.UUID;

public record CursorPageResponse<T>(
        List<T> data,
        String nextCursor,
        UUID nextIdAfter,
        String sortBy,
        String sortDirection,
        Long totalCount,
        boolean hasNext
) {}