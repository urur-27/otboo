package com.team3.otboo.domain.clothing.repository;

import com.team3.otboo.domain.clothing.entity.Clothing;
import com.team3.otboo.domain.user.entity.User;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClothingRepository extends JpaRepository<Clothing, UUID>, ClothingRepositoryCustom {
    long countByImageId(UUID imageId);

  @EntityGraph(attributePaths = {"attributeValues,", "attributeValues.attribute", "attributeValues.option"})
  List<Clothing> findByOwner(User owner);
}
