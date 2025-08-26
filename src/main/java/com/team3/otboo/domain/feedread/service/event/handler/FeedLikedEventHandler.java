package com.team3.otboo.domain.feedread.service.event.handler;

import com.team3.otboo.common.event.Event;
import com.team3.otboo.common.event.EventType;
import com.team3.otboo.common.event.payload.FeedLikedEventPayload;
import com.team3.otboo.domain.feedread.repository.FeedQueryModelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("feedReadFeedLikedEventHandler")
@RequiredArgsConstructor
public class FeedLikedEventHandler implements EventHandler<FeedLikedEventPayload> {

	private final FeedQueryModelRepository feedQueryModelRepository;

	@Override
	public void handle(Event<FeedLikedEventPayload> event) {
		FeedLikedEventPayload payload = event.getPayload();
		// 여기서 동시성 문제가 발생
		feedQueryModelRepository.read(payload.getFeedId())
			.ifPresent(feedQueryModel -> {
				feedQueryModel.updateBy(payload);
				feedQueryModelRepository.update(feedQueryModel);
			});
	}

	@Override
	public boolean supports(Event<FeedLikedEventPayload> event) {
		return event.getType() == EventType.FEED_LIKED;
	}
}
