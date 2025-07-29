package com.team3.otboo.domain.follow.dto;

import com.team3.otboo.domain.follow.entity.Follow;
import com.team3.otboo.domain.user.dto.UserSummary;
import java.util.UUID;

public record FollowDto(
	UUID id,
	UserSummary followee,
	UserSummary follower
) {

	public static FollowDto of(
		Follow follow,
		UserSummary followeeUserSummary,
		UserSummary followerUserSummary) {

		return new FollowDto(
			follow.getId(),
			followeeUserSummary,
			followerUserSummary
		);
	}
}
