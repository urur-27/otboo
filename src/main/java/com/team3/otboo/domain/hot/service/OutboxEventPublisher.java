package com.team3.otboo.domain.hot.service;

import com.team3.otboo.common.event.EventType;
import com.team3.otboo.common.outboxMessageRelay.Outbox;
import com.team3.otboo.common.outboxMessageRelay.OutboxEvent;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxEventPublisher {

	// 스프링 내부에 이벤트 시스템을 통해서 발행함 .
	private final ApplicationEventPublisher applicationEventPublisher;

	// 카프카로 바로 전송하지 않고 .. MessageRelay(중계 시스템) 에게 publish 함 .
	// MessageRelay 는 @TransactionalEventListener 를 통해 듣고 있다가 . 이벤트가 발생하면 적절한 처리를 한다 .
	public void publish(EventType eventType, String payload, UUID shardKey) {
		Outbox outbox = Outbox.create(
			eventType,
			payload,
			shardKey
		);
		// 이벤트가 발생했다는 신호를 애플리케이션에 전파함 .
		applicationEventPublisher.publishEvent(OutboxEvent.of(outbox));
	}
}
