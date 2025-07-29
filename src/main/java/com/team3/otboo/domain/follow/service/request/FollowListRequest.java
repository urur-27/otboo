package com.team3.otboo.domain.follow.service.request;

import java.time.LocalDateTime;
import java.util.UUID;

public record FollowListRequest(
	UUID userId,
	LocalDateTime cursor,
	UUID idAfter,
	int limit,
	String nameLike
) {

}
