package com.team3.otboo.domain.feedread.consumer;

import com.team3.otboo.common.event.EventType.Topic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FeedReadEventConsumer {

	@KafkaListener(
		topics = {
			Topic.OTBOO_FEED,
			Topic.OTBOO_FEED_COMMENT,
			Topic.OTBOO_FEED_LIKE
		}
	)
	public void listen(String message, Acknowledgment ack) {
		log.info("[FeedReadEventConsumer.listen] message = {}", message);
	}
}
