package com.team3.otboo.domain.hot.service.event.handler;

import com.team3.otboo.common.event.Event;
import com.team3.otboo.common.event.EventType;
import com.team3.otboo.common.event.payload.FeedCreatedEventPayload;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component("hotFeedCreatedEventHandler")
@RequiredArgsConstructor
@Slf4j
public class FeedCreatedEventHandler implements EventHandler<FeedCreatedEventPayload> {

	@Override
	public void handle(Event<FeedCreatedEventPayload> event) {
		log.info("[FeedCreatedEventHandler.handle] hot feed 에 대한 데이터 생성");
		// MSA 에서 hot feed 에 대한 정보를 .따로 repository 에 저장할때 사용 .
		// FeedCommentCount, FeedLikeCount, feed 의 생성 시간 등 ..
	}

	@Override
	public boolean supports(Event<FeedCreatedEventPayload> event) {
		return event.getType() == EventType.FEED_CREATED;
	}

	@Override
	public UUID findFeedId(Event<FeedCreatedEventPayload> event) {
		return event.getPayload().getId();
	}
}
