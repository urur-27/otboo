package com.team3.otboo.domain.clothing.repository;

import com.team3.otboo.domain.clothing.dto.response.CursorPageResponse;
import com.team3.otboo.domain.clothing.entity.Clothing;
import java.util.UUID;
import org.springframework.data.domain.Sort;

public interface ClothingRepositoryCustom {
    CursorPageResponse<Clothing> findAllByCursor(
            UUID ownerId,
            String cursor,
            UUID idAfter,
            int limit,
            String typeEqual,
            Sort.Direction direction
    );
}
