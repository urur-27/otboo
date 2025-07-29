package com.team3.otboo.domain.follow.entity;

import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Table(name = "user_following_count")
public class UserFollowingCount {

	@Id
	private UUID userId;
	private Long followingCount;

	public static UserFollowingCount init(UUID userId, Long followingCount) {
		UserFollowingCount userFollowingCount = new UserFollowingCount();
		userFollowingCount.userId = userId;
		userFollowingCount.followingCount = followingCount;
		return userFollowingCount;
	}
}
