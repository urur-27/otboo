package com.team3.otboo.domain.feed.service.response;

import com.team3.otboo.domain.feed.dto.FeedDto;
import com.team3.otboo.domain.user.enums.SortDirection;
import java.util.List;
import java.util.UUID;

public record FeedDtoCursorResponse(
	List<FeedDto> data,
	String nextCursor,
	UUID nextIdAfter,
	boolean hasNext,
	int totalCount,
	String sortBy,
	SortDirection sortDirection
) {

}
