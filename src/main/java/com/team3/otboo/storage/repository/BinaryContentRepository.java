package com.team3.otboo.storage.repository;

import com.team3.otboo.domain.user.entity.Profile;
import com.team3.otboo.storage.entity.BinaryContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BinaryContentRepository extends JpaRepository<BinaryContent, UUID> {
    Boolean existsBinaryContentById(UUID uuid);
}
