package com.team3.otboo.domain.feed.dto;

import java.util.List;
import java.util.UUID;
import org.hibernate.query.SortDirection;

public record CommentDtoCursorResponse(
	List<CommentDto> data,
	String nextCursor,
	UUID nextIdAfter,
	boolean hasNext,
	int totalCount,
	String sortBy,
	SortDirection sortDirection
) {

}
