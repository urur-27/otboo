package com.team3.otboo.domain.feedread.service.event.handler;

import com.team3.otboo.common.event.Event;
import com.team3.otboo.common.event.EventType;
import com.team3.otboo.common.event.payload.FeedDeletedEventPayload;
import com.team3.otboo.domain.feedread.repository.FeedIdListRepository;
import com.team3.otboo.domain.feedread.repository.FeedQueryModelRepository;
import com.team3.otboo.domain.feedread.repository.FeedSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("feedReadFeedDeletedEventHandler")
@RequiredArgsConstructor
public class FeedDeletedEventHandler implements EventHandler<FeedDeletedEventPayload> {

	private final FeedQueryModelRepository feedQueryModelRepository;
	private final FeedIdListRepository feedIdListRepository;

	private final FeedSearchRepository feedSearchRepository;

	@Override
	public void handle(Event<FeedDeletedEventPayload> event) {
		FeedDeletedEventPayload payload = event.getPayload();
		feedQueryModelRepository.delete(payload.getId());
		feedIdListRepository.delete(payload.getId());

		feedSearchRepository.deleteById(payload.getId());
	}

	@Override
	public boolean supports(Event<FeedDeletedEventPayload> event) {
		return EventType.FEED_DELETE == event.getType();
	}
}
