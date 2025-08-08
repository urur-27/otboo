package com.team3.otboo.domain.hot.service.eventhandler;

import com.team3.otboo.common.event.Event;
import com.team3.otboo.common.event.EventType;
import com.team3.otboo.common.event.payload.FeedLikeEventPayload;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

@Component
@Repository
public class FeedLikeEventHandler implements EventHandler<FeedLikeEventPayload> {

	@Override
	public void handle(Event<FeedLikeEventPayload> event) {
	}

	@Override
	public boolean supports(Event<FeedLikeEventPayload> event) {
		return event.getType() == EventType.FEED_LIKED;
	}

	@Override
	public UUID findFeedId(Event<FeedLikeEventPayload> event) {
		return event.getPayload().getId();
	}
}
