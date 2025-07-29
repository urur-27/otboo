package com.team3.otboo.domain.follow.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Table(name = "user_follower_count")
@Entity
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserFollowerCount {

	@Id
	private UUID userId;
	private Long followerCount;

	public static UserFollowerCount init(UUID userId, Long followerCount) {
		UserFollowerCount userFollowerCount = new UserFollowerCount();
		userFollowerCount.userId = userId;
		userFollowerCount.followerCount = followerCount;

		return userFollowerCount;
	}
}
