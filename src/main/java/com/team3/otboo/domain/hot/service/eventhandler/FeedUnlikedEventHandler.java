package com.team3.otboo.domain.hot.service.eventhandler;

import com.team3.otboo.domain.hot.common.event.Event;
import com.team3.otboo.domain.hot.common.event.EventType;
import com.team3.otboo.domain.hot.common.event.payload.FeedUnLikeEventPayload;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeedUnlikedEventHandler implements EventHandler<FeedUnLikeEventPayload> {

	@Override
	public void handle(Event<FeedUnLikeEventPayload> event) {

	}

	@Override
	public boolean supports(Event<FeedUnLikeEventPayload> event) {
		return event.getType() == EventType.FEED_UNLIKED;
	}

	@Override
	public UUID findFeedId(Event<FeedUnLikeEventPayload> event) {
		return event.getPayload().getId();
	}
}
