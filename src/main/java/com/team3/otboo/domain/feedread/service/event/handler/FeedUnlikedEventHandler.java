package com.team3.otboo.domain.feedread.service.event.handler;

import com.team3.otboo.common.event.Event;
import com.team3.otboo.common.event.EventType;
import com.team3.otboo.common.event.payload.FeedUnlikedEventPayload;
import com.team3.otboo.domain.feedread.repository.FeedQueryModelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("feedReadFeedUnlikedEventHandler")
@RequiredArgsConstructor
public class FeedUnlikedEventHandler implements EventHandler<FeedUnlikedEventPayload> {

	private final FeedQueryModelRepository feedQueryModelRepository;

	@Override
	public void handle(Event<FeedUnlikedEventPayload> event) {
		FeedUnlikedEventPayload payload = event.getPayload();
		feedQueryModelRepository.read(payload.getFeedId())
			.ifPresent(feedQueryModel -> {
				feedQueryModel.updateBy(payload);
				feedQueryModelRepository.update(feedQueryModel);
			});
	}

	@Override
	public boolean supports(Event<FeedUnlikedEventPayload> event) {
		return event.getType() == EventType.FEED_UNLIKED;
	}
}
