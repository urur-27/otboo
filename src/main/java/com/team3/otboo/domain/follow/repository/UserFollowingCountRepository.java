package com.team3.otboo.domain.follow.repository;

import com.team3.otboo.domain.follow.entity.UserFollowingCount;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserFollowingCountRepository extends JpaRepository<UserFollowingCount, UUID> {

	@Query(
		value = "update user_following_count set following_count = following_count + 1 where user_id = :userId",
		nativeQuery = true
	)
	@Modifying
	int increase(@Param("userId") UUID userId);

	@Query(
		value = "update user_following_count set following_count = following_count - 1 where user_id = :userId",
		nativeQuery = true
	)
	@Modifying
	int decrease(@Param("userId") UUID userId);


}
