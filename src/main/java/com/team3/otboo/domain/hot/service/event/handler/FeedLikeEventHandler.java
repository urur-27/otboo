package com.team3.otboo.domain.hot.service.event.handler;

import com.team3.otboo.common.event.Event;
import com.team3.otboo.common.event.EventType;
import com.team3.otboo.common.event.payload.FeedLikedEventPayload;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

@Component("hotFeedLikedEventHandler")
@Repository
public class FeedLikeEventHandler implements EventHandler<FeedLikedEventPayload> {

	@Override
	public void handle(Event<FeedLikedEventPayload> event) {
	}

	@Override
	public boolean supports(Event<FeedLikedEventPayload> event) {
		return event.getType() == EventType.FEED_LIKED;
	}

	@Override
	public UUID findFeedId(Event<FeedLikedEventPayload> event) {
		return event.getPayload().getId();
	}
}
