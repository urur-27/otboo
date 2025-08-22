package com.team3.otboo.domain.hot.consumer;

import com.team3.otboo.common.event.Event;
import com.team3.otboo.common.event.EventPayload;
import com.team3.otboo.common.event.EventType.Topic;
import com.team3.otboo.domain.hot.service.HotFeedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class HotFeedEventConsumer {

	private final HotFeedService hotFeedService;

	// 현재는 통합 consumer 방식이지만, 나중에 view 서비스 같은 부분에서 처리량이 너무 많아진다면,
	// view topic 에 대한 consumer 를 따로 분리할 수도 있음 .
	@KafkaListener(
		topics = {
			Topic.OTBOO_FEED,
			Topic.OTBOO_FEED_COMMENT,
			Topic.OTBOO_FEED_LIKE,
			Topic.OTBOO_FEED_VIEW
		},
		groupId = "hot-feed-group"
	)
	public void listen(String message, Acknowledgment ack) {
		log.info("[HotArticleEventConsumer.listen] received message = {}", message);
		Event<EventPayload> event = Event.fromJson(message);

		if (event != null) {
			hotFeedService.handleEvent(event); // hot feed service 는 event handler 를 가지고 있음 .
		}
		ack.acknowledge();
	}
}
