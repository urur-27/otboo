package com.team3.otboo.domain.feedread.service.event.handler;

import com.team3.otboo.common.event.Event;
import com.team3.otboo.common.event.EventType;
import com.team3.otboo.common.event.payload.FeedUpdatedEventPayload;
import com.team3.otboo.domain.feedread.repository.FeedQueryModelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("feedReadFeedUpdatedEventHandler")
@RequiredArgsConstructor
public class FeedUpdatedEventHandler implements EventHandler<FeedUpdatedEventPayload> {

	private final FeedQueryModelRepository feedQueryModelRepository;

	@Override
	public void handle(Event<FeedUpdatedEventPayload> event) {
		FeedUpdatedEventPayload payload = event.getPayload();
		feedQueryModelRepository.read(payload.getId())
			.ifPresent(feedQueryModel -> {
				feedQueryModel.updateBy(payload);
				feedQueryModelRepository.update(feedQueryModel);
			});
	}

	@Override
	public boolean supports(Event<FeedUpdatedEventPayload> event) {
		return event.getType() == EventType.FEED_UPDATED;
	}
}
