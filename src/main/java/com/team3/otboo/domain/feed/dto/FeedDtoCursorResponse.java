package com.team3.otboo.domain.feed.dto;

import java.util.List;
import java.util.UUID;
import org.hibernate.query.SortDirection;

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
