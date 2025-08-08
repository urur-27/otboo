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

	// String 으로 된 이벤트 받아서 객체로 바꾸고, hotFeedService 의 handle 메서드로 넘김 ..
	@KafkaListener(topics = {
		Topic.OTBOO_FEED,
		Topic.OTBOO_FEED_COMMENT,
		Topic.OTBOO_FEED_LIKE,
		Topic.OTBOO_FEED_VIEW
	})
	public void listen(String message, Acknowledgment ack) {
		log.info("[HotArticleEventConsumer.listen] received message = {}", message);
		Event<EventPayload> event = Event.fromJson(message);

		if (event != null) {
			hotFeedService.handleEvent(event); // hot feed service 는 event handler 를 가지고 있음 .
		}
		ack.acknowledge();
	}
}
