package com.team3.otboo.domain.feedread.client;

import com.team3.otboo.domain.feed.dto.FeedDto;
import com.team3.otboo.domain.feed.service.FeedService;
import com.team3.otboo.domain.feed.service.request.FeedListRequest;
import com.team3.otboo.domain.feed.service.response.FeedDtoCursorResponse;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeedClient {

	private final FeedService feedService;

	public Optional<FeedDto> read(UUID feedId, UUID userId) {
		return Optional.of(feedService.read(feedId, userId));
	}

	// redis 에 없으면 RDBMS 에서 가져오기 .
	public Optional<FeedDtoCursorResponse> readAllInfiniteScroll(UUID userId,
		FeedListRequest request) {
		return Optional.of(feedService.readAllInfiniteScroll(userId, request));
	}
}
