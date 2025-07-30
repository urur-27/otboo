package com.team3.otboo.domain.clothing.repository;

import com.team3.otboo.domain.clothing.entity.AttributeOption;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttributeOptionRepository extends JpaRepository<AttributeOption, UUID> {

    Optional<AttributeOption> findByAttributeIdAndValue(UUID attributeId, String value);
}