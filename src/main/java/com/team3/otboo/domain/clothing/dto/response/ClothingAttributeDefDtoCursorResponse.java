package com.team3.otboo.domain.clothing.dto.response;

import com.team3.otboo.domain.clothing.dto.ClothingAttributeDefDto;
import java.util.List;
import java.util.UUID;

public record ClothingAttributeDefDtoCursorResponse(
        List<ClothingAttributeDefDto> data,
        String nextCursor,
        UUID nextIdAfter,
        boolean hasNext,
        long totalCount,
        String sortBy,
        String sortDirection
) {}