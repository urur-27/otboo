package com.team3.otboo.domain.clothing.repository;

import com.team3.otboo.domain.clothing.dto.response.CursorPageResponse;
import com.team3.otboo.domain.clothing.entity.Attribute;
import org.springframework.data.domain.Sort;

public interface AttributeRepositoryCustom {
    CursorPageResponse<Attribute> findAllByCursor(
            String cursor,
            int limit,
            String sortBy,
            Sort.Direction direction,
            String keyword
    );
}
