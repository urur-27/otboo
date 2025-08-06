package com.team3.otboo.domain.follow.service.request;

import com.team3.otboo.domain.follow.dto.FollowDto;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record FollowListResponse(
	List<FollowDto> data,
	UUID userId,
	LocalDateTime cursor,
	UUID idAfter,
	int limit,
	String nameLike
) {

}
