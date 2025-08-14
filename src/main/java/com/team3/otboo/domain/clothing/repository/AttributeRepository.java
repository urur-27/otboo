package com.team3.otboo.domain.clothing.repository;

import com.team3.otboo.domain.clothing.entity.Attribute;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AttributeRepository extends JpaRepository<Attribute, UUID>, AttributeRepositoryCustom {
    boolean existsByName(String name);

    // N+1 방지: 옵션까지 fetch join
    @Query("""
      select distinct a from Attribute a
      left join fetch a.options
    """)
    List<Attribute> findAllWithOptions();
}