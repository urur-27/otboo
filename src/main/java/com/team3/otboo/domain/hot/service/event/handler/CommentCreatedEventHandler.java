package com.team3.otboo.domain.hot.service.event.handler;

import com.team3.otboo.common.event.Event;
import com.team3.otboo.common.event.EventType;
import com.team3.otboo.common.event.payload.CommentCreatedEventPayload;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("hotCommentCreatedEventHandler")
@RequiredArgsConstructor
public class CommentCreatedEventHandler implements EventHandler<CommentCreatedEventPayload> {

	@Override
	public void handle(Event<CommentCreatedEventPayload> event) {
	}

	@Override
	public boolean supports(Event<CommentCreatedEventPayload> event) {
		return event.getType() == EventType.COMMENT_CREATED;
	}

	@Override
	public UUID findFeedId(Event<CommentCreatedEventPayload> event) {
		return event.getPayload().getFeedId();
	}
}
