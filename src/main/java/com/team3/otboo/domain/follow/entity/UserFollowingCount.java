package com.team3.otboo.domain.follow.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Table(name = "user_following_count")
@Entity
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
