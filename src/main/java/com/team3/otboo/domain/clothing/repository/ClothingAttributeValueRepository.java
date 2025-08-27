package com.team3.otboo.domain.clothing.repository;

import com.team3.otboo.domain.clothing.entity.ClothingAttributeValue;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClothingAttributeValueRepository extends
        JpaRepository<ClothingAttributeValue, UUID> {
    long countByOption_Id(UUID optionId);
}
