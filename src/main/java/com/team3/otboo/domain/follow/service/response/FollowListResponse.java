package com.team3.otboo.domain.follow.service.response;

import com.team3.otboo.domain.follow.dto.FollowDto;
import java.util.List;
import java.util.UUID;
import org.hibernate.query.SortDirection;

public record FollowListResponse(
	List<FollowDto> data, // 그냥 follow dto 를 보냄 .
	String nextCursor,
	UUID nextIdAfter,
	boolean hasNext,
	int totalCount,
	String sortBy,
	SortDirection sortDirection
) {

}
