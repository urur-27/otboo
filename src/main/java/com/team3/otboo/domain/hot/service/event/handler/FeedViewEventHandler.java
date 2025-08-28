package com.team3.otboo.domain.hot.service.event.handler;

import com.team3.otboo.common.event.Event;
import com.team3.otboo.common.event.EventType;
import com.team3.otboo.common.event.payload.FeedViewedEventPayload;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component("hotFeedViewedEventHandler")
@RequiredArgsConstructor
public class FeedViewEventHandler implements EventHandler<FeedViewedEventPayload> {

	@Override
	public void handle(Event<FeedViewedEventPayload> event) {
	}

	@Override
	public boolean supports(Event<FeedViewedEventPayload> event) {
		return event.getType() == EventType.FEED_VIEWED;
	}

	@Override
	public UUID findFeedId(Event<FeedViewedEventPayload> event) {
		return event.getPayload().getFeedId();
	}
}
