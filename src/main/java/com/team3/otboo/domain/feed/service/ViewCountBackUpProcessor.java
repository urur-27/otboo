package com.team3.otboo.domain.feed.service;

import com.team3.otboo.common.event.EventType;
import com.team3.otboo.common.event.payload.FeedViewedEventPayload;
import com.team3.otboo.domain.feed.entity.FeedViewCount;
import com.team3.otboo.domain.feed.repository.FeedViewCountBackUpRepository;
import com.team3.otboo.domain.hot.service.HotFeedOutboxEventPublisher;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ViewCountBackUpProcessor {

	private final HotFeedOutboxEventPublisher hotFeedOutboxEventPublisher;
	private final FeedViewCountBackUpRepository feedViewCountBackUpRepository;

	private final HotFeedOutboxEventPublisher outboxEventPublisher;

	@Transactional
	public void backUp(UUID feedId, Long viewCount) {
		int result = feedViewCountBackUpRepository.update(feedId, viewCount);
		if (result == 0) {
			feedViewCountBackUpRepository.findById(feedId)
				.ifPresentOrElse(ignored -> {
					}, // 재확인 로직 .. 그사이에 누가 만들었을 수도 있으니까. (경쟁 상태 방어)
					() -> feedViewCountBackUpRepository.save(FeedViewCount.init(feedId, viewCount))
				);
		}

		outboxEventPublisher.publish(
			EventType.FEED_VIEWED,
			FeedViewedEventPayload.builder()
				.feedId(feedId)
				.feedViewCount(viewCount)
				.build()
		);
	}
}
