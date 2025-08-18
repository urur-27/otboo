package com.team3.otboo.domain.feedread.client;

import com.team3.otboo.domain.feed.service.FeedService;
import com.team3.otboo.domain.feed.service.response.FeedResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeedClient {

	private final FeedService feedService;

	public FeedResponse read(UUID feedId) {
		return FeedResponse.from(feedService.read(feedId));
	}
}
