package com.team3.otboo.domain.hot.service;

import com.team3.otboo.common.event.Event;
import com.team3.otboo.common.event.EventPayload;
import com.team3.otboo.domain.feed.entity.Feed;
import com.team3.otboo.domain.feed.service.FeedService;
import com.team3.otboo.domain.hot.repository.HotFeedListRepository;
import com.team3.otboo.domain.hot.service.event.handler.EventHandler;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HotFeedScoreUpdater {

	private final FeedService feedService;

	private final HotFeedScoreCalculator hotFeedScoreCalculator;
	private final HotFeedListRepository hotFeedListRepository;

	private static final long HOT_FEED_COUNT = 5; // hot feed 5개
	private static final Duration HOT_FEED_TTL = Duration.ofDays(10); // 10일 저장 ..

	public void update(Event<EventPayload> event, EventHandler<EventPayload> eventHandler) {
		UUID feedId = eventHandler.findFeedId(event);
		Feed feed = feedService.read(feedId);

		if (!isFeedCreatedToday(feed.getCreatedAt())) {
			return;
		}

		eventHandler.handle(event);

		// 댓글 생성, 좋아요 생성 삭제 때마다 score 를 갱신해줘야함 .. -> 이런거 부하가 심하지 않나 .
		long score = hotFeedScoreCalculator.calculate(feedId);
		hotFeedListRepository.add(
			feedId,
			feed.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDateTime(),
			score,
			HOT_FEED_COUNT,
			HOT_FEED_TTL
		);
	}

	// 한국 시간대로 변경해서 비교함 .
	private boolean isFeedCreatedToday(Instant createdAt) {
		LocalDate today = LocalDate.now();
		LocalDate createdDate = createdAt.atZone(ZoneId.systemDefault()).toLocalDate();
		return today.equals(createdDate);
	}
}
