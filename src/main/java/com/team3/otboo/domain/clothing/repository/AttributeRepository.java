package com.team3.otboo.domain.clothing.repository;

import com.team3.otboo.domain.clothing.entity.Attribute;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttributeRepository extends JpaRepository<Attribute, UUID> {}