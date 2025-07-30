package com.team3.otboo.domain.clothing.repository;

import com.team3.otboo.domain.clothing.entity.Clothing;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClothingRepository extends JpaRepository<Clothing, UUID> {
}
