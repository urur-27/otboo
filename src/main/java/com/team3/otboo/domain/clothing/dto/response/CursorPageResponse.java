package com.team3.otboo.domain.clothing.dto.response;

import java.util.List;

public record CursorPageResponse<T>(
        List<T> data,
        String nextCursor,
        String sortBy,
        String sortDirection,
        Long totalCount
) {}