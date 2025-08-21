package com.team3.otboo.domain.hot.service.event.handler;

import com.team3.otboo.common.event.Event;
import com.team3.otboo.common.event.EventType;
import com.team3.otboo.common.event.payload.FeedDeletedEventPayload;
import com.team3.otboo.domain.hot.repository.HotFeedListRepository;
import java.time.ZoneId;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("hotFeedDeletedEventHandler")
@RequiredArgsConstructor
public class FeedDeletedEventHandler implements EventHandler<FeedDeletedEventPayload> {

	private final HotFeedListRepository hotFeedListRepository;

	@Override
	public void handle(Event<FeedDeletedEventPayload> event) {
		FeedDeletedEventPayload payload = event.getPayload();
		hotFeedListRepository.remove(
			payload.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDateTime(),
			payload.getId()
		);
	}

	@Override
	public boolean supports(Event<FeedDeletedEventPayload> event) {
		return event.getType() == EventType.FEED_DELETE;
	}

	@Override
	public UUID findFeedId(Event<FeedDeletedEventPayload> event) {
		return event.getPayload().getId();
	}
}
