package com.team3.otboo.domain.user.repository;

import com.team3.otboo.domain.user.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, UUID>{
    Optional<Profile> findByUser_Id(UUID userId);

    @Query("""
        select p from Profile p
        join fetch p.user u
        where p.location.x = :x and p.location.y = :y
      """)
    List<Profile> findAllByGrid(@Param("x") int x, @Param("y") int y);
}
