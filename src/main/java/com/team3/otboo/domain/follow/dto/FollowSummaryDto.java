package com.team3.otboo.domain.follow.dto;

import java.util.UUID;

public record FollowSummaryDto(
	UUID followeeId,
	int followerCount,
	int followingCount,
	boolean followedByMe,
	UUID followedByMeId, // 사용자가 조회 대상을 팔로우 했을때 팔로우 객체의 아이디 .
	boolean followingMe

) {

}
