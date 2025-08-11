package com.team3.otboo.domain.hot.client;

import com.team3.otboo.domain.feed.dto.FeedDto;
import com.team3.otboo.domain.feed.service.FeedService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeedClient {

	// 피드 서비스를 통해 피드 원본 정보를 호출함 .
	private final FeedService feedService;

	public FeedDto read(UUID feedId, UUID userId) {
		return feedService.read(feedId, userId);
	}
}
