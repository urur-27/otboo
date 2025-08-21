package com.team3.otboo.domain.feedread.service.event.handler;

import com.team3.otboo.common.event.Event;
import com.team3.otboo.common.event.EventType;
import com.team3.otboo.common.event.payload.CommentCreatedEventPayload;
import com.team3.otboo.domain.feedread.repository.FeedQueryModelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("feedReadCommentCreatedEventHandler")
@RequiredArgsConstructor
public class CommentCreatedEventHandler implements EventHandler<CommentCreatedEventPayload> {

	private final FeedQueryModelRepository feedQueryModelRepository;

	@Override
	public void handle(Event<CommentCreatedEventPayload> event) {
		CommentCreatedEventPayload payload = event.getPayload();
		feedQueryModelRepository.read(payload.getFeedId())
			.ifPresent(feedQueryModel -> {
				feedQueryModel.updateBy(payload);
				feedQueryModelRepository.update(feedQueryModel);
			});
	}

	@Override
	public boolean supports(Event<CommentCreatedEventPayload> event) {
		return event.getType() == EventType.COMMENT_CREATED;
	}
}
