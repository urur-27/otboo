package com.team3.otboo.domain.user.dto.response;

import com.team3.otboo.domain.user.dto.UserDto;
import com.team3.otboo.domain.user.enums.SortBy;
import com.team3.otboo.domain.user.enums.SortDirection;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
public class UserDtoCursorResponse {

    private List<UserDto> data;
    private String nextCursor;
    private UUID nextIdAfter;
    private boolean hasNext;
    private long totalCount;
    private SortBy sortBy;
    private SortDirection sortDirection;

    @Builder
    private UserDtoCursorResponse(List<UserDto> data, String nextCursor, UUID nextIdAfter, boolean hasNext, long totalCount, SortBy sortBy, SortDirection sortDirection) {
        this.data = data;
        this.nextCursor = nextCursor;
        this.nextIdAfter = nextIdAfter;
        this.hasNext = hasNext;
        this.totalCount = totalCount;
        this.sortBy = sortBy;
        this.sortDirection = sortDirection;
    }

    public static UserDtoCursorResponse of(List<UserDto> data, String nextCursor, UUID nextIdAfter, boolean hasNext, long totalCount, SortBy sortBy, SortDirection sortDirection) {
        return UserDtoCursorResponse.builder()
                .data(data)
                .nextCursor(nextCursor)
                .nextIdAfter(nextIdAfter)
                .hasNext(hasNext)
                .totalCount(totalCount)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();
    }
}