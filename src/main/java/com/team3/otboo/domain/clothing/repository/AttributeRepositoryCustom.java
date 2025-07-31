package com.team3.otboo.domain.clothing.repository;

import com.team3.otboo.domain.clothing.dto.response.CursorPageResponse;
import com.team3.otboo.domain.clothing.entity.Attribute;
import com.team3.otboo.domain.clothing.service.Direction;
import java.util.UUID;

public interface AttributeRepositoryCustom {
    CursorPageResponse<Attribute> findAllByCursor(
            String cursor, UUID idAfter, int limit, String sortBy, Direction direction, String keyword
    );

}
