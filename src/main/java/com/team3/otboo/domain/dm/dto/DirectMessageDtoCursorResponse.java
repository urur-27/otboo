package com.team3.otboo.domain.dm.dto;

import java.util.List;
import java.util.UUID;
import org.hibernate.query.SortDirection;

public record DirectMessageDtoCursorResponse(
	List<DirectMessageDto> data,
	String nextCursor,
	UUID nextIdAfter,
	boolean hasNext,
	int totalCount,
	String sortBy,
	SortDirection sortDirection
) {

}
