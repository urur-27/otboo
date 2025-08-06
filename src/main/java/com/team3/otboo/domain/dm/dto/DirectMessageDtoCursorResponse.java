package com.team3.otboo.domain.dm.dto;

import java.util.List;
import java.util.UUID;
import org.hibernate.query.SortDirection;

public record DirectMessageDtoCursorResponse(
	List<DirectMessageDto> data,
	String nextCursor,
	UUID nextIdAfter,
	boolean hasNext,
	int totalCount, // 두 사용자 간의 전체 DM 개수
	String sortBy,
	SortDirection sortDirection
) {

}
