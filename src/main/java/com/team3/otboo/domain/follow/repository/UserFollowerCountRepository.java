package com.team3.otboo.domain.follow.repository;

import com.team3.otboo.domain.follow.entity.UserFollowerCount;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserFollowerCountRepository extends JpaRepository<UserFollowerCount, UUID> {

	@Query(
		value = "update user_follower_count set follower_count = follower_count + 1 where userId = :userId",
		nativeQuery = true
	)
	@Modifying
	int increase(@Param("userId") UUID userId);

	@Query(
		value = "update user_following_count set following_count = following_count + 1 where userId = :userId",
		nativeQuery = true
	)
	@Modifying
	int decrease(@Param("userId") UUID userId);
}
