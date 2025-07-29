package com.team3.otboo.domain.clothing.service;

import com.team3.otboo.domain.clothing.dto.ClothingAttributeDefDto;
import com.team3.otboo.domain.clothing.dto.request.ClothingAttributeDefCreateRequest;
import com.team3.otboo.domain.clothing.dto.response.CursorPageResponse;
import java.util.UUID;


public interface ClothingAttributeDefService {
    ClothingAttributeDefDto create(ClothingAttributeDefCreateRequest request);

    CursorPageResponse<ClothingAttributeDefDto> getAttributes(
            String cursor,
            int limit,
            String sortBy,
            String sortDirection,
            String keyword
    );

    void deleteAttribute(UUID definitionId);
}
