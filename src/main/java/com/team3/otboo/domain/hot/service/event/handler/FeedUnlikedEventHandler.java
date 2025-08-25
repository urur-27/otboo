package com.team3.otboo.domain.hot.service.event.handler;

import com.team3.otboo.common.event.Event;
import com.team3.otboo.common.event.EventType;
import com.team3.otboo.common.event.payload.FeedUnlikedEventPayload;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("hotFeedUnlikedEventHandler")
@RequiredArgsConstructor
public class FeedUnlikedEventHandler implements EventHandler<FeedUnlikedEventPayload> {

	@Override
	public void handle(Event<FeedUnlikedEventPayload> event) {

	}

	@Override
	public boolean supports(Event<FeedUnlikedEventPayload> event) {
		return event.getType() == EventType.FEED_UNLIKED;
	}

	@Override
	public UUID findFeedId(Event<FeedUnlikedEventPayload> event) {
		return event.getPayload().getFeedId();
	}
}
