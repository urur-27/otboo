package com.team3.otboo.domain.clothing.service;

import com.team3.otboo.domain.clothing.dto.ClothesAttributeDefDto;
import com.team3.otboo.domain.clothing.dto.request.ClothesAttributeDefCreateRequest;
import com.team3.otboo.domain.clothing.dto.response.CursorPageResponse;
import java.util.UUID;


public interface ClothingAttributeDefService {
    ClothesAttributeDefDto create(ClothesAttributeDefCreateRequest request);

    CursorPageResponse<ClothesAttributeDefDto> getAttributes(
            String cursor,
            UUID idAfter,
            int limit,
            String sortBy,
            Direction direction,
            String keyword
    );

    void deleteAttribute(UUID definitionId);

    ClothesAttributeDefDto updateAttribute(UUID definitionId, ClothesAttributeDefCreateRequest request);
}
