package com.team3.otboo.domain.follow.service.request;

import java.util.UUID;

public record FollowCreateRequest(
	UUID followeeId,
	UUID followerId
) {
}
