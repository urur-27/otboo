package com.team3.otboo.domain.clothing.dto.response;

import com.team3.otboo.domain.clothing.dto.ClothesDto;
import java.util.List;
import java.util.UUID;

public record ClothingDtoCursorResponse(
        List<ClothesDto> data,
        String nextCursor,
        UUID nextIdAfter,
        boolean hasNext,
        long totalCount,
        String sortBy,
        String sortDirection
) {}
