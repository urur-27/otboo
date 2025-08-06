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

	// 대규모 트래픽이 예상되는 상황에서 팔로워, 팔로잉 수를 세는데 count 쿼리를 사용하면 너무 오래결럼 UserFollowerCount, UserFollowingCount 라는 객체를 따로 저장
	@Query(
		value = "update user_follower_count set follower_count = follower_count + 1 where user_id = :userId",
		nativeQuery = true
	)
	@Modifying
	int increase(@Param("userId") UUID userId);

	@Query(
		value = "update user_following_count set following_count = following_count -1 where user_id = :userId",
		nativeQuery = true
	)
	@Modifying
	int decrease(@Param("userId") UUID userId);
}
